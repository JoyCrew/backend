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

  /**
   * 서비스 이용 가능 기한(마지막 날의 23:59:59 같은 느낌)
   * null이면 아직 결제/구독 시작 전 상태로 취급
   */
  @Column(name = "subscription_end_at")
  private LocalDateTime subscriptionEndAt;

  @Builder.Default
  @OneToMany(mappedBy = "company")
  private List<Employee> employees = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Department> departments = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CompanyAdminAccess> adminAccessList = new ArrayList<>();

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public void changeName(String newCompanyName) {
    this.companyName = newCompanyName;
  }

  public void changeStatus(String newStatus) {
    this.status = newStatus;
  }

  public void addBudget(double amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Budget amount cannot be negative.");
    }
    this.totalCompanyBalance += amount;
  }

  public void spendBudget(double amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount to spend cannot be negative.");
    }
    if (this.totalCompanyBalance < amount) {
      throw new InsufficientPointsException("The company does not have enough budget to distribute the points.");
    }
    this.totalCompanyBalance -= amount;
  }

  /**
   * 구독 기간 연장
   * - subscriptionEndAt이 미래면 그 날짜 기준으로 연장
   * - 아니면 현재 시간을 기준으로 연장
   * - 연장 후 상태도 자동 업데이트
   */
  public void extendSubscription(int months) {
    if (months <= 0) {
      throw new IllegalArgumentException("months must be positive");
    }

    LocalDateTime base =
            (this.subscriptionEndAt != null && this.subscriptionEndAt.isAfter(LocalDateTime.now()))
                    ? this.subscriptionEndAt
                    : LocalDateTime.now();

    this.subscriptionEndAt = base.plusMonths(months);

    refreshStatusBySubscription();
  }

  /**
   * 구독 만료일에 따라 회사 상태 갱신
   */
  public void refreshStatusBySubscription() {
    if (this.subscriptionEndAt == null) {
      // 최초 결제 전이라면 ACTIVE 유지 (원하면 PENDING 등으로 커스터마이징 가능)
      return;
    }

    if (this.subscriptionEndAt.isAfter(LocalDateTime.now())) {
      this.status = "ACTIVE";
    } else {
      this.status = "EXPIRED";
    }
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = this.updatedAt = LocalDateTime.now();
    if (this.totalCompanyBalance == null) {
      this.totalCompanyBalance = 0.0;
    }
    if (this.status == null) {
      this.status = "ACTIVE";
    }
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
