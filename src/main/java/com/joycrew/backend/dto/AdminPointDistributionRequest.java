package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminPointDistributionRequest(
        @NotEmpty(message = "Employee ID list cannot be empty.")
        List<Long> employeeIds,

        @NotNull(message = "Points are required.")
        int points,

        @NotNull(message = "Message is required.")
        String message,

        @NotNull(message = "Transaction type is required.")
        TransactionType type
) {}