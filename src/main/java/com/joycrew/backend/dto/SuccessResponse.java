package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful Operation Response DTO")
public record SuccessResponse(
        @Schema(description = "Success message", example = "The operation was completed successfully.")
        String message
) {
    public static SuccessResponse defaultSuccess() {
        return new SuccessResponse("Processed successfully.");
    }
}