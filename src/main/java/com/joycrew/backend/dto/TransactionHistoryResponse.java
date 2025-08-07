package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TransactionHistoryResponse(
        Long transactionId,
        TransactionType type,
        int amount,
        String counterparty,
        String message,
        LocalDateTime transactionDate
) {}