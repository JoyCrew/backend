package com.joycrew.backend.entity;

import com.joycrew.backend.exception.InsufficientPointsException;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long walletId;

  @OneToOne
  @JoinColumn(name = "employee_id", nullable = false, unique = true)
  private Employee employee;

  @Column(nullable = false)
  private Integer balance;

  @Column(nullable = false)
  private Integer giftablePoint;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public Wallet(Employee employee) {
    this.employee = employee;
    this.balance = 0;
    this.giftablePoint = 0;
  }

  public void addPoints(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Points to add cannot be negative.");
    }
    this.balance += amount;
    this.giftablePoint += amount;
  }

  public void spendGiftablePoints(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Points to spend cannot be negative.");
    }
    if (this.giftablePoint < amount) {
      throw new InsufficientPointsException("Insufficient giftable points.");
    }
    this.balance -= amount;
    this.giftablePoint -= amount;
  }

  /**
   * 개인 구매용 결제: balance 에서만 차감한다.
   * giftablePoint 는 선물 가능 한도로 유지한다.
   */
  public void purchaseWithPoints(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Purchase amount cannot be negative.");
    }
    if (this.balance < amount) {
      throw new InsufficientPointsException("Insufficient points for purchase.");
    }
    this.balance -= amount; // ✅ 구매는 balance만 차감
    // ✅ giftablePoint는 변경하지 않음
  }

  public void revokePoints(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount to revoke cannot be negative.");
    }
    if (this.balance < amount) {
      throw new InsufficientPointsException("Insufficient balance to revoke points.");
    }
    this.balance -= amount;
    this.giftablePoint = Math.max(0, this.giftablePoint - amount);
  }

  public void refundPoints(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Refund amount cannot be negative.");
    }
    this.balance += amount;
    this.giftablePoint += amount;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = this.updatedAt = LocalDateTime.now();
    if (this.balance == null) this.balance = 0;
    if (this.giftablePoint == null) this.giftablePoint = 0;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
