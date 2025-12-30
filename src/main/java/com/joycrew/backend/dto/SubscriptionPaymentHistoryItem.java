package com.joycrew.backend.dto;

import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.entity.enums.PaymentStatus;

import java.time.LocalDateTime;

public record SubscriptionPaymentHistoryItem(
        Long id,
        String orderId,
        Long amount,
        PaymentStatus status,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime periodStartAt,
        LocalDateTime periodEndAt,
        String tossPaymentKey,
        String failCode,
        String failMessage
) {
    public static SubscriptionPaymentHistoryItem from(SubscriptionPayment p) {
        return new SubscriptionPaymentHistoryItem(
                p.getId(),
                p.getOrderId(),
                Long.valueOf(p.getAmount()),
                p.getStatus(),
                p.getRequestedAt(),
                p.getApprovedAt(),
                p.getPeriodStartAt(),
                p.getPeriodEndAt(),
                p.getTossPaymentKey(),
                p.getFailCode(),
                p.getFailMessage()
        );
    }
}
