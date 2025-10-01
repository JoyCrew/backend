package com.joycrew.backend.entity;

import com.joycrew.backend.exception.InsufficientPointsException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    // ⭐️ 동시성 제어를 위한 낙관적 락 필드 추가
    @Version
    private Long version;

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
        // ⭐️ 잔액 확인 강화 (비관적 락과 함께 동시성 문제 해결)
        if (this.giftablePoint < amount) {
            throw new InsufficientPointsException("Insufficient giftable points.");
        }
        this.balance -= amount;
        this.giftablePoint -= amount;
    }

    public void purchaseWithPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative.");
        }
        if (this.balance < amount) {
            throw new InsufficientPointsException("Insufficient points for purchase.");
        }
        this.balance -= amount;

        // ⭐️ giftablePoint 불일치 문제 해소:
        // 최소 0 보장 (기존 Math.max 로직을 유지하면서 안전성 확보)
        this.giftablePoint -= amount;
        if (this.giftablePoint < 0) {
            this.giftablePoint = 0;
        }
    }

    public void revokePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to revoke cannot be negative.");
        }
        if (this.balance < amount) {
            throw new InsufficientPointsException("Insufficient balance to revoke points.");
        }
        this.balance -= amount;
        // 기존 로직 유지
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