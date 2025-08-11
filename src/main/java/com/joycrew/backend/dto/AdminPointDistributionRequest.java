package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminPointDistributionRequest(
        @NotEmpty(message = "Distribution list cannot be empty.")
        @Size(min = 1, message = "At least one employee must be selected.")
        @Valid
        List<PointDistributionDetail> distributions,

        @NotNull(message = "Message is required.")
        String message,

        @NotNull(message = "Transaction type is required.")
        TransactionType type
) {}