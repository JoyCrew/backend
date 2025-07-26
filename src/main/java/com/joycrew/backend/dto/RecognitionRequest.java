package com.joycrew.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecognitionRequest(
        @NotNull Long receiverId,
        @Min(1) int points,
        @Size(max = 255) String message
) {}