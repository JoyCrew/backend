package com.joycrew.backend.dto.kyc;

import jakarta.validation.constraints.NotBlank;

public record PhoneVerifyRequest(
        @NotBlank String requestId,
        @NotBlank String code
) {}