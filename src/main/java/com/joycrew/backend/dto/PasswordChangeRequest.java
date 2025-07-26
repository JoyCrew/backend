package com.joycrew.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public record PasswordChangeRequest(
        @NotBlank(message = "새로운 비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상 20자 이하이어야 합니다.")
        String newPassword
) {}