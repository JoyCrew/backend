package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wallet Balance Response DTO")
public record PointBalanceResponse(
  @Schema(description = "Current total balance") Integer totalBalance,
  @Schema(description = "Current giftable points") Integer giftableBalance
) {}