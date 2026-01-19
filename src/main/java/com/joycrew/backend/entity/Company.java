package com.joycrew.backend.entity;

import com.joycrew.backend.exception.InsufficientPointsException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long companyId;

  private String companyName;
  private String status;
  private LocalDateTime startAt;

  @Column(nullable = false)
  private Double totalCompanyBalance;

  @Builder.Default
  @OneToMany(mappedBy = "company")
  private List<Employee> employees = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Department> departments = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CompanyAdminAccess> adminAccessList = new ArrayList<>();

  // =========================
  // Subscription fields
  // =========================

  @Column(name = "subscription_end_at")
  private LocalDateTime subscriptionEndAt;

  @Column(nullable=false)
  private boolean autoRenew;

  @Column(name = "toss_billing_key")
  private String tossBillingKey;

  @Column(name = "toss_customer_key")
  private String tossCustomerKey;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // -----------------------
  // Budget Logic (기존)
  // -----------------------

  public void addBudget(double amount) {
    if (amount < 0) throw new IllegalArgumentException("Budget amount cannot be negative.");
    if (this.totalCompanyBalance == null) this.totalCompanyBalance = 0.0;
    this.totalCompanyBalance += amount;
  }

  public void spendBudget(double amount) {
    if (amount < 0) throw new IllegalArgumentException("Amount to spend cannot be negative.");
    if (this.totalCompanyBalance == null) this.totalCompanyBalance = 0.0;
    if (this.totalCompanyBalance < amount) {
      throw new InsufficientPointsException("The company does not have enough budget to distribute the points.");
    }
    this.totalCompanyBalance -= amount;
  }

  public Double getTotalCompanyBalance() {
    return this.totalCompanyBalance == null ? 0.0 : this.totalCompanyBalance;
  }

  // -----------------------
  // Subscription Logic
  // -----------------------

  /** billingKey 저장 + autoRenew ON */
  public void registerBillingKeyAndEnableAutoRenew(String billingKey, String customerKey) {
    this.autoRenew = true;
    this.tossBillingKey = billingKey;
    this.tossCustomerKey = customerKey;
  }

  /** ✅ 최초 카드등록 시점 기준으로 만료일을 now+1개월로 세팅 (이미 있으면 절대 덮어쓰지 않음) */
  public void initializeSubscriptionEndAtIfFirstTime() {
    if (this.subscriptionEndAt == null) {
      this.subscriptionEndAt = LocalDateTime.now().plusMonths(1);
      this.status = "ACTIVE";
    }
  }

  /** 자동갱신 해지 */
  public void disableAutoRenew() {
    this.autoRenew = false;
    // 보안상 권장: 해지하면 키도 제거 (원하면 아래 2줄 삭제)
    this.tossBillingKey = null;
    this.tossCustomerKey = null;
  }

  public boolean canAutoBill() {
    return autoRenew && tossBillingKey != null && subscriptionEndAt != null;
  }

  /** 결제 성공 시 구독 연장 */
  public void extendSubscription(int months) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime base =
            (subscriptionEndAt != null && subscriptionEndAt.isAfter(now))
                    ? subscriptionEndAt
                    : now;
    subscriptionEndAt = base.plusMonths(months);
    status = "ACTIVE";
  }

  public void markFailed() {
    status = "PAYMENT_FAILED";
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = this.updatedAt = LocalDateTime.now();
    if (this.totalCompanyBalance == null) this.totalCompanyBalance = 0.0;
    if (this.status == null) this.status = "ACTIVE";
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public boolean isBillingReady() {
    // 카드 등록이 끝났다는 의미: billingKey + customerKey 존재
    return this.tossBillingKey != null && this.tossCustomerKey != null && !this.tossBillingKey.isBlank();
  }
}
