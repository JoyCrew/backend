package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
  @Schema(description = "Password reset token received via email")
  @NotBlank(message = "Token is required.")
  String token,

  @Schema(description = "The new password")
  @NotBlank(message = "New password is required.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
      message = "Password must be 8-20 characters long and include at least one letter, one number, and one special character.")
  String newPassword
) {}