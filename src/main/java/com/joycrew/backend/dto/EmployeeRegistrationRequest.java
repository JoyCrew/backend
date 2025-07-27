package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeRegistrationRequest (
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "초기 비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    String initialPassword,

    @NotNull(message = "회사 ID는 필수입니다.")
    Long companyId,

    Long departmentId,

    @NotBlank(message = "직책은 필수입니다.")
    String position,

    @NotNull(message = "역할은 필수입니다.")
    UserRole role
){}
