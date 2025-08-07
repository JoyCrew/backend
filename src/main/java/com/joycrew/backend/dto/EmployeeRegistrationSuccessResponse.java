package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee Creation Success Response DTO")
public record EmployeeRegistrationSuccessResponse(
        @Schema(example = "Employee created successfully (ID: 2)", description = "Response message")
        String message
) {}