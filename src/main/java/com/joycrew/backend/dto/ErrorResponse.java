package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Error response")
public record ErrorResponse(
  @Schema(description = "Error code", example = "INSUFFICIENT_POINTS")
  String code,
  @Schema(description = "Error message", example = "Not enough points to complete the purchase.")
  String message,
  @Schema(description = "Timestamp", example = "2025-08-11T10:45:00")
  LocalDateTime timestamp,
  @Schema(description = "Request path", example = "/api/orders")
  String path
) { }
