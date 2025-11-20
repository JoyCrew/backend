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
    private Integer balance; // 의미: 총 잔액

    @Column(nullable = false)
    private Integer giftablePoint; // 의미: 총 잔액 중 선물 가능한 한도

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
     * [NEW] 2. P2P 선물 받기
     * 동료에게 받은 선물은 '총 잔액(balance)'만 올리고 '선물 한도(giftablePoint)'는 건드리지 않습니다.
     * (즉, '구매 전용' 포인트가 됨)
     */
    public void receiveGiftPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Points to receive cannot be negative.");
        }
        this.balance += amount;
        // giftablePoint는 올리지 않음
    }

    /**
     * [3. P2P 선물 하기]
     * 선물을 하면 총 잔액과 선물 한도 둘 다 차감합니다. (기존 로직 유지)
     */
    public void spendGiftablePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Points to spend cannot be negative.");
        }
        if (this.giftablePoint < amount) {
            throw new InsufficientPointsException("Insufficient giftable points.");
        }
        if (this.balance < amount) {
            // (방어 코드) 총액이 선물 한도보다 적은 비정상적 상황
            throw new InsufficientPointsException("Insufficient balance.");
        }
        this.balance -= amount;
        this.giftablePoint -= amount;
    }

    /**
     * [FIXED] 4. 기프티콘 구매
     * 구매 시 '총 잔액(balance)'만 차감합니다. '선물 한도(giftablePoint)'는 건드리지 않습니다.
     */
    public void purchaseWithPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative.");
        }
        if (this.balance < amount) {
            throw new InsufficientPointsException("Insufficient points for purchase.");
        }
        this.balance -= amount;

        // [수정됨] 선물 한도를 차감하는 로직 삭제
        // this.giftablePoint = Math.max(0, this.giftablePoint - amount);
    }

    /**
     * [5. 관리자 회수]
     * 관리자가 회수할 때는 총 잔액과 선물 한도 둘 다 차감합니다.
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
     * [FIXED] 6. 구매 환불
     * 구매가 실패하여 환불될 때는 '총 잔액(balance)'만 다시 채워줍니다.
     */
    public void refundPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Refund amount cannot be negative.");
        }
        this.balance += amount;
    }

    // --- (PrePersist, PreUpdate는 기존과 동일) ---
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