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
        String profileImageUrl,
        @Schema(description = "Tenant subdomain (e.g., 'alko', 'BDL')")
        String subdomain,
        @Schema(description = "Whether billing method registration is required after login")
        boolean billingRequired
) {
    public static LoginResponse fail(String message) {
        return new LoginResponse(
                null,          // accessToken
                message,        // message
                null,          // userId
                null,          // name
                null,          // email
                null,          // role
                null,          // totalPoint
                null,          // profileImageUrl
                null,          // subdomain
                false          // billingRequired (로그인 실패면 의미 없으니 false)
        );
    }
}
