package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.AdminLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO")
public record LoginResponse(
        @Schema(description = "JWT 토큰")
        String accessToken,
        @Schema(description = "응답 메시지")
        String message,
        @Schema(description = "사용자 고유 ID")
        Long userId,
        @Schema(description = "사용자 이름")
        String name,
        @Schema(description = "사용자 이메일")
        String email,
        @Schema(description = "사용자 역할")
        AdminLevel role,
        @Schema(description = "보유 포인트")
        Integer totalPoint,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl
) {
        public static LoginResponse fail(String message) {
                return new LoginResponse(null, message, null, null, null, null, null, null);
        }
}