package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @Schema(description = "Email of the account to reset the password for", example = "user@example.com")
        @NotBlank(message = "Email is required.")
        @Email(message = "Must be a valid email format.")
        String email
) {}