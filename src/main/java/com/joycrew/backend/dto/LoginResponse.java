package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO", example = "{ \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\" }")
public class LoginResponse {

    @Schema(description = "JWT 토큰 또는 에러 메시지", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
}
