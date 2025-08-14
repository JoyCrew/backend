package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Admin-specific Employee Search Response DTO")
public record AdminEmployeeQueryResponse(
  @Schema(description = "Employee ID", example = "1")
  Long employeeId,

  @Schema(description = "Employee name", example = "John Doe")
  String employeeName,

  @Schema(description = "Employee email", example = "john.doe@example.com")
  String email,

  @Schema(description = "Department name", example = "Engineering")
  String departmentName,

  @Schema(description = "Position or title", example = "Backend Developer")
  String position,

  @Schema(description = "URL of the profile image")
  String profileImageUrl,

  @Schema(description = "Employee role/permission level", example = "HR_ADMIN")
  String adminLevel,

  @Schema(description = "Phone number", example = "010-1234-5678")
  String phoneNumber,

  @Schema(description = "Birth date", example = "1995-05-10")
  LocalDate birthday,

  @Schema(description = "Address", example = "123 Teheran-ro, Gangnam-gu, Seoul")
  String address,

  @Schema(description = "Hire date", example = "2023-01-10")
  LocalDate hireDate
) {}