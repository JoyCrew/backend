package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee Search Result DTO")
public record EmployeeQueryResponse(
        @Schema(description = "Unique ID of the employee", example = "1")
        Long employeeId,

        @Schema(description = "URL of the profile image", example = "https://cdn.joycrew.com/profile/user123.jpg")
        String profileImageUrl,

        @Schema(description = "Name of the employee", example = "John Doe")
        String employeeName,

        @Schema(description = "Department name", example = "Engineering")
        String departmentName,

        @Schema(description = "Position or title", example = "Backend Developer")
        String position
) {}