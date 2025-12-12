package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.SubscriptionPaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_subscription_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CompanySubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * 신청 개월 수
     */
    @Column(nullable = false)
    private int months;

    /**
     * 결제 금액(원) – months * 50_000
     */
    @Column(nullable = false)
    private int amount;

    /**
     * NHN KCP 측 주문번호(our side에서 만들어 보내는 값)
     */
    @Column(nullable = false, unique = true)
    private String orderId;

    /**
     * KCP 거래번호 (tid 같은 것)
     */
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPaymentStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime failedAt;

    @Column(length = 1000)
    private String failReason;

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = SubscriptionPaymentStatus.REQUESTED;
        }
    }

    public void markSuccess(String transactionId) {
        this.transactionId = transactionId;
        this.status = SubscriptionPaymentStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
    }

    public void markFail(String reason) {
        this.status = SubscriptionPaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failReason = reason;
    }
}
