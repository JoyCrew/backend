package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for responding with the admin's personal wallet balance and the company's total point budget.
 */
@Schema(description = "Admin's point budget and personal balance response DTO")
public record AdminPointBudgetResponse(
        @Schema(description = "The total point budget available for the entire company")
        Double companyTotalBalance,

        @Schema(description = "The admin's personal total point balance")
        Integer adminPersonalTotalBalance,

        @Schema(description = "The admin's personal giftable point balance")
        Integer adminPersonalGiftableBalance
) {}
