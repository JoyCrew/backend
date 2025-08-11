package com.joycrew.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PointDistributionDetail(
        @NotNull(message = "Employee ID is required.")
        Long employeeId,

        @NotNull(message = "Points are required.")
        @Min(value = 1, message = "Points must be at least 1.")
        Integer points
) {}