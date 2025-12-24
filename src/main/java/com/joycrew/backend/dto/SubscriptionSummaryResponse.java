package com.joycrew.backend.dto;

import java.time.LocalDateTime;

public record SubscriptionSummaryResponse(
        LocalDateTime subscriptionStartAt, // 가입일
        LocalDateTime nextBillingAt,        // 결제 예정일
        boolean autoRenew,
        String status
) {}
