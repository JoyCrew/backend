package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
        @Schema(description = "이메일로 받은 비밀번호 재설정 토큰")
        @NotBlank(message = "토큰은 필수입니다.")
        String token,

        @Schema(description = "새로운 비밀번호")
        @NotBlank(message = "새로운 비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상 20자 이하여야 합니다.")
        String newPassword
) {
}