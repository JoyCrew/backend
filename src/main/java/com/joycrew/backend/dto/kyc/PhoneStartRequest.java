package com.joycrew.backend.dto.kyc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneStartRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,11}$") String phone
) {}