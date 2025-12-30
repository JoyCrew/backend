package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "subscription_payment",
        indexes = {
                @Index(name = "idx_subpay_company_requested", columnList = "company_id, requested_at"),
                @Index(name = "idx_subpay_company_paid", columnList = "company_id, approved_at"),
                @Index(name = "idx_subpay_order", columnList = "order_id", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 결제 대상 회사 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** 우리쪽 주문 ID (멱등성/조회 기준) */
    @Column(name = "order_id", nullable = false, unique = true, length = 80)
    private String orderId;

    /** 토스 paymentKey (성공 시 응답에 존재) */
    @Column(name = "toss_payment_key", length = 200)
    private String tossPaymentKey;

    /** 금액 */
    @Column(nullable = false)
    private long amount;

    /** 이번 결제가 커버하는 구독 기간 */
    @Column(name = "period_start_at", nullable = false)
    private LocalDateTime periodStartAt;

    @Column(name = "period_end_at", nullable = false)
    private LocalDateTime periodEndAt;

    /** 결제 요청 시각 */
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    /** 승인 시각 */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /** 실패 사유 */
    @Column(name = "fail_reason", length = 500)
    private String failReason;

    /** 토스 응답 raw */
    @Lob
    @Column(name = "raw_response")
    private String rawResponse;

    public static SubscriptionPayment pending(Company company,
                                              String orderId,
                                              long amount,
                                              LocalDateTime periodStartAt,
                                              LocalDateTime periodEndAt,
                                              LocalDateTime requestedAt) {
        SubscriptionPayment p = new SubscriptionPayment();
        p.company = company;
        p.orderId = orderId;
        p.amount = amount;
        p.periodStartAt = periodStartAt;
        p.periodEndAt = periodEndAt;
        p.requestedAt = requestedAt;
        p.status = PaymentStatus.PENDING;
        return p;
    }

    public void markSuccess(String tossPaymentKey, LocalDateTime approvedAt, String rawResponse) {
        this.status = PaymentStatus.SUCCESS;
        this.tossPaymentKey = tossPaymentKey;
        this.approvedAt = approvedAt;
        this.failReason = null;
        this.rawResponse = rawResponse;
    }

    public void markFailed(String reason, String rawResponse) {
        this.status = PaymentStatus.FAILED;
        this.failReason = reason;
        this.rawResponse = rawResponse;
    }

    // ======================
    // DTO/프론트 호환 getter
    // ======================

    public String getFailCode() {
        return null; // 지금은 코드 저장 안 하므로 null (추후 확장)
    }

    public String getFailMessage() {
        return this.failReason;
    }
}
