package com.joycrew.backend.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for verifying the current password")
public record PasswordVerifyRequest(
        @Schema(description = "The user's current password", example = "password123!")
        @NotBlank(message = "Current password is required.")
        String currentPassword
) {}