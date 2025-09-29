package com.joycrew.backend.dto.kyc;

public record PhoneStartResponse(
        String requestId,
        int resendAvailableInSec
) {}