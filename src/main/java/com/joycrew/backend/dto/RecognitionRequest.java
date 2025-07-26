package com.joycrew.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecognitionRequest(
        @NotNull(message = "받는 사람 ID는 필수입니다.") Long receiverId,
        @NotNull(message = "포인트는 필수입니다.") @Min(value = 1, message = "포인트는 1 이상이어야 합니다.") int points,
        @Size(max = 255, message = "메시지는 255자를 초과할 수 없습니다.") String message
) {}