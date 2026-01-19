package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_payment",
        indexes = {
                @Index(name = "idx_subscription_payment_order_id", columnList = "order_id", unique = true),
                @Index(name = "idx_subscription_payment_company_id", columnList = "company_id"),
                @Index(name = "idx_subscription_payment_status", columnList = "status")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 회사 결제인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 멱등성 key (회사+기간 기준으로 고정 생성)
    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    // 결제 금액 (예: 50000)
    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    // ✅ "요청 생성 시각" (DTO의 requestedAt 대응)
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    // ✅ Toss 승인 시각(성공 시)
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // 이번 결제가 커버하는 구독 기간
    @Column(name = "period_start_at", nullable = false)
    private LocalDateTime periodStartAt;

    @Column(name = "period_end_at", nullable = false)
    private LocalDateTime periodEndAt;

    // ✅ Toss 결제키(성공 시 저장) - DTO의 tossPaymentKey 대응
    @Column(name = "toss_payment_key")
    private String tossPaymentKey;

    // ✅ 실패코드/메시지 - DTO의 failMessage 대응
    @Column(name = "fail_code")
    private String failCode;

    @Column(name = "fail_message", length = 500)
    private String failMessage;

    // 디버깅/감사 목적: Toss 응답 원문(일부) 저장
    @Lob
    @Column(name = "raw_response")
    private String rawResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // -----------------------
    // Factory
    // -----------------------
    public static SubscriptionPayment pending(
            Company company,
            String orderId,
            long amount,
            LocalDateTime periodStartAt,
            LocalDateTime periodEndAt,
            LocalDateTime now
    ) {
        return SubscriptionPayment.builder()
                .company(company)
                .orderId(orderId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .requestedAt(now)
                .periodStartAt(periodStartAt)
                .periodEndAt(periodEndAt)
                .build();
    }

    // -----------------------
    // State transitions
    // -----------------------
    public void markSuccess(String tossPaymentKey, LocalDateTime approvedAt, String rawResponse) {
        this.status = PaymentStatus.SUCCESS;
        this.tossPaymentKey = tossPaymentKey;
        this.approvedAt = approvedAt != null ? approvedAt : LocalDateTime.now();
        this.rawResponse = rawResponse;
        this.failCode = null;
        this.failMessage = null;
    }

    public void markFailed(String failCode, String failMessageOrRaw) {
        this.status = PaymentStatus.FAILED;
        this.failCode = failCode;
        this.failMessage = failMessageOrRaw;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.requestedAt == null) this.requestedAt = now;
        if (this.status == null) this.status = PaymentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
