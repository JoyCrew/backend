package com.joycrew.backend.dto.kyc;

public record PhoneVerifyResponse(
        boolean verified,
        String kycToken
) {}