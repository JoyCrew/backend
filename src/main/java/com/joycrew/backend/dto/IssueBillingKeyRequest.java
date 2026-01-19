package com.joycrew.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueBillingKeyRequest(
        @NotBlank String authKey
) {}
