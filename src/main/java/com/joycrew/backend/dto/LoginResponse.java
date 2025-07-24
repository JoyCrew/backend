package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder; // @Builder 어노테이션 추가
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {

    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "응답 메시지 (성공/실패)", example = "로그인 성공" )
    private String message;

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 역할", example = "EMPLOYEE", allowableValues = {"EMPLOYEE", "MANAGER", "HR_ADMIN", "SUPER_ADMIN"})
    private UserRole role; // ENUM 타입 유지
}