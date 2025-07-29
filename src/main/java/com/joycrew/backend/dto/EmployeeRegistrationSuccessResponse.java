package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직원 생성 성공 응답 DTO")
public record EmployeeRegistrationSuccessResponse(
        @Schema(example = "직원 생성 완료 (ID: 2)", description = "응답 메시지")
        String message
) {}
