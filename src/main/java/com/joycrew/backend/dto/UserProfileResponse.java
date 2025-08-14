package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.AdminLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "User Profile Response DTO")
public record UserProfileResponse(
  @Schema(description = "Unique ID of the user") Long employeeId,
  @Schema(description = "Name of the user") String name,
  @Schema(description = "Email address of the user") String email,
  @Schema(description = "URL of the profile image") String profileImageUrl,
  @Schema(description = "Current total point balance") Integer totalBalance,
  @Schema(description = "Current giftable point balance") Integer giftableBalance,
  @Schema(description = "Role or permission level of the user") AdminLevel level,
  @Schema(description = "Department name") String department,
  @Schema(description = "Position or title of the user") String position,
  @Schema(description = "Phone number of the user") String phoneNumber,
  @Schema(description = "Birth date of the user") LocalDate birthday,
  @Schema(description = "Address of the user") String address,
  @Schema(description = "Hire date of the user") LocalDate hireDate
) {}