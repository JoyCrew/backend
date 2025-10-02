package com.joycrew.backend.dto.kyc;

import java.util.List;

public record PhoneVerifyResponse(
        boolean verified,
        String kycToken,
        List<String> emails,
        String recentLoginAt
) {}