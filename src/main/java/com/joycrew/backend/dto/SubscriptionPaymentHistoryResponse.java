package com.joycrew.backend.dto;

import java.util.List;

public record SubscriptionPaymentHistoryResponse(
        List<SubscriptionPaymentHistoryItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
