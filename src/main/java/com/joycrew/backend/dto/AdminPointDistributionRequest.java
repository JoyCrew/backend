package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminPointDistributionRequest(
        @NotEmpty(message = "직원 ID 목록은 비어있을 수 없습니다.")
        List<Long> employeeIds,

        @NotNull(message = "포인트는 필수입니다.")
        int points,

        @NotNull(message = "메시지는 필수입니다.")
        String message,

        @NotNull(message = "거래 유형은 필수입니다.")
        TransactionType type
) {}