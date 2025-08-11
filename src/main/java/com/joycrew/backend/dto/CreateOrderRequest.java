package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create order request")
public record CreateOrderRequest(
        @Schema(description = "Product ID", example = "101")
        Long productId,
        @Schema(description = "Quantity", example = "2")
        Integer quantity
) { }
