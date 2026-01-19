package com.joycrew.backend.dto;

import com.joycrew.backend.entity.SubscriptionPayment;
import org.springframework.data.domain.Page;

import java.util.List;

public record SubscriptionPaymentHistoryResponse(
        List<SubscriptionPaymentHistoryItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /** ✅ QueryService에서 호출하는 팩토리 메서드 */
    public static SubscriptionPaymentHistoryResponse from(Page<SubscriptionPayment> page) {
        return new SubscriptionPaymentHistoryResponse(
                page.getContent().stream()
                        .map(SubscriptionPaymentHistoryItem::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
