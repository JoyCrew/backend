package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(
                description = "Fixed to AWARD_MANAGER_SPOT for admin bulk distributions",
                allowableValues = {"AWARD_MANAGER_SPOT"},
                example = "AWARD_MANAGER_SPOT",
                defaultValue = "AWARD_MANAGER_SPOT"
        )
        TransactionType type
) { }
