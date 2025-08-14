package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.AdminLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRegistrationRequest (
  @NotBlank(message = "Name is required.")
  String name,

  @NotBlank(message = "Email is required.")
  @Email(message = "Must be a valid email format.")
  String email,

  @NotBlank(message = "Initial password is required.")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  String initialPassword,

  @NotBlank(message = "Company name is required.")
  String companyName,

  String departmentName,

  @NotBlank(message = "Position is required.")
  String position,

  @NotNull(message = "Role is required.")
  AdminLevel level,

  @Schema(description = "Phone number", example = "010-1234-5678")
  String phoneNumber,

  LocalDate birthday,

  String address,

  LocalDate hireDate
) {}