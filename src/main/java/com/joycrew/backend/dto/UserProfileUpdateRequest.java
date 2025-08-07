package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "User Profile Update Request DTO")
public record UserProfileUpdateRequest(
        @Schema(description = "The employee's new name", example = "John Doe")
        String name,

        @Schema(description = "The new profile image URL")
        String profileImageUrl,

        @Schema(description = "The new personal email address")
        String personalEmail,

        @Schema(description = "The new phone number")
        String phoneNumber,

        @Schema(description = "The new birth date")
        LocalDate birthday,

        @Schema(description = "The new address")
        String address
) {}