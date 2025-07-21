package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

    @Schema(description = "이메일 주소", example = "user@example.com", required = true)
    @Email
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password123!", required = true)
    @NotBlank
    private String password;
}
