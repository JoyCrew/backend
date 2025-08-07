package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Email address", example = "user@example.com")
        @Email(message = "Must be a valid email format.")
        @NotBlank(message = "Email is required.")
        String email,

        @Schema(description = "Password", example = "password123!")
        @NotBlank(message = "Password is required.")
        String password
) {}