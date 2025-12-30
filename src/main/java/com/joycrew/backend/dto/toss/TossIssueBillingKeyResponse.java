package com.joycrew.backend.dto.toss;

public record TossIssueBillingKeyResponse(
        String mId,
        String customerKey,
        String billingKey,
        String authenticatedAt,
        String cardCompany,
        String cardNumber,
        String cardType
) {}
