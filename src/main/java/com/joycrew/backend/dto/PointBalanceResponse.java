package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Wallet;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지갑 잔액 응답 DTO")
public record PointBalanceResponse(
        @Schema(description = "현재 잔액") Integer totalBalance,
        @Schema(description = "선물 가능한 포인트") Integer giftableBalance
) {
}