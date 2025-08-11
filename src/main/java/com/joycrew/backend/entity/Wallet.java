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

    public void spendPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Points to spend cannot be negative.");
        }
        if (this.balance < amount || this.giftablePoint < amount) {
            throw new InsufficientPointsException("Insufficient giftable points.");
        }
        this.balance -= amount;
        this.giftablePoint -= amount;
    }

    // Refund purchase points back to wallet
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
