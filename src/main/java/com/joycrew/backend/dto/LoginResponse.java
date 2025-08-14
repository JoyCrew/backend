package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.AdminLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login Response DTO")
public record LoginResponse(
  @Schema(description = "JWT access token")
  String accessToken,
  @Schema(description = "Response message")
  String message,
  @Schema(description = "Unique ID of the user")
  Long userId,
  @Schema(description = "Name of the user")
  String name,
  @Schema(description = "Email of the user")
  String email,
  @Schema(description = "Role of the user")
  AdminLevel role,
  @Schema(description = "Total points balance")
  Integer totalPoint,
  @Schema(description = "URL of the profile image")
  String profileImageUrl
) {
  public static LoginResponse fail(String message) {
    return new LoginResponse(null, message, null, null, null, null, null, null);
  }
}