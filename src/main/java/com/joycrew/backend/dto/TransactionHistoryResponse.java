package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Detailed transaction history item")
public record TransactionHistoryResponse(
        @Schema(description = "Transaction ID")
        Long transactionId,

        @Schema(description = "Transaction type")
        TransactionType type,

        @Schema(description = "Point amount (negative for sent/redeemed points)")
        int amount,

        @Schema(description = "Counterparty's name or system message")
        String counterparty,

        @Schema(description = "Transaction message")
        String message,

        @Schema(description = "Transaction date and time")
        LocalDateTime transactionDate,

        @Schema(description = "Counterparty's profile image URL (if applicable)")
        String counterpartyProfileImageUrl,

        @Schema(description = "Counterparty's department name (if applicable)")
        String counterpartyDepartmentName
) {}