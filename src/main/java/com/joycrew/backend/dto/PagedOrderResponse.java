package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paged order response")
public record PagedOrderResponse(
        @Schema(description = "Content list")
        List<OrderResponse> content,
        @Schema(description = "Current page (0-based)", example = "0")
        int page,
        @Schema(description = "Page size", example = "20")
        int size,
        @Schema(description = "Total elements", example = "12")
        long totalElements,
        @Schema(description = "Total pages", example = "2")
        int totalPages,
        @Schema(description = "Has next page", example = "true")
        boolean hasNext,
        @Schema(description = "Has previous page", example = "false")
        boolean hasPrevious
) { }
