package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Recently viewed product item")
public record RecentViewedProductResponse(
        @Schema(description = "Product")
        ProductResponse product,
        @Schema(description = "Viewed at", example = "2025-08-11T12:34:56")
        LocalDateTime viewedAt
) {
    public static RecentViewedProductResponse of(Product product, LocalDateTime viewedAt) {
        return new RecentViewedProductResponse(ProductResponse.from(product), viewedAt);
    }
}
