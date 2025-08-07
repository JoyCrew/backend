package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record UserProfileUpdateRequest(
        @Schema(description = "변경할 직원 이름", example = "김조이")
        String name,

        @Schema(description = "변경할 프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "변경할 개인 이메일")
        String personalEmail,

        @Schema(description = "변경할 연락처")
        String phoneNumber,

        @Schema(description = "변경할 생년월일")
        LocalDate birthday,

        @Schema(description = "변경할 주소")
        String address
) {}