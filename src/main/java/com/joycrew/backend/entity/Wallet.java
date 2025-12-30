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
    private Integer balance;          // 의미: 총 잔액

    @Column(nullable = false)
    private Integer giftablePoint;    // 의미: 총 잔액 중 선물 가능한 한도

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Wallet(Employee employee) {
        this.employee = employee;
        this.balance = 0;
        this.giftablePoint = 0;
    }

    /**
     * [1. 관리자 충전]
     * 관리자가 준 포인트는 총 잔액과 선물 한도를 둘 다 올립니다. (기존 로직 유지)
     */
    public void addPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Points to add cannot be negative.");
        }
        this.balance += amount;
        this.giftablePoint += amount;
    }

    /**
     * [2. P2P 선물 받기]
     * 동료에게 받은 선물은 '총 잔액(balance)'만 올리고
     * '선물 한도(giftablePoint)'는 건드리지 않습니다.
     * -> 선물로 받은 포인트는 "구매 전용" 느낌.
     */
    public void receiveGiftPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Points to receive cannot be negative.");
        }
        this.balance += amount;
        // giftablePoint는 증가시키지 않음
    }

    /**
     * [3. P2P 선물 하기]
     * 선물을 하면 총 잔액과 선물 한도 둘 다 차감합니다.
     */
    public void spendGiftablePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Points to spend cannot be negative.");
        }
        if (this.giftablePoint < amount) {
            throw new InsufficientPointsException("Insufficient giftable points.");
        }
        if (this.balance < amount) {
            // 방어 코드: 총 잔액이 선물 한도보다 적은 비정상 상황
            throw new InsufficientPointsException("Insufficient balance.");
        }
        this.balance -= amount;
        this.giftablePoint -= amount;
    }

    /**
     * [4. 기프티콘/스토어 구매]
     * 구매 시 '총 잔액(balance)'만 차감합니다.
     * '선물 한도(giftablePoint)'는 건드리지 않습니다.
     */
    public void purchaseWithPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative.");
        }
        if (this.balance < amount) {
            throw new InsufficientPointsException("Insufficient points for purchase.");
        }
        this.balance -= amount;
        // giftablePoint는 변화 없음
    }

    /**
     * [5. 관리자 회수]
     * 관리자가 포인트를 회수할 때는 총 잔액과 선물 한도 둘 다 차감합니다.
     */
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

    /**
     * [6. 구매 환불]
     * 구매 실패/취소로 인한 환불은 '총 잔액(balance)'만 다시 채워줍니다.
     * 선물 한도는 변하지 않습니다.
     */
    public void refundPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Refund amount cannot be negative.");
        }
        this.balance += amount;
        // giftablePoint는 변화 없음
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
