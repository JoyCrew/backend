package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Product;
import com.joycrew.backend.entity.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Product summary DTO")
public record ProductResponse(
  @Schema(description = "Product ID", example = "1")
  Long id,
  @Schema(description = "Category (keyword)", example = "BEAUTY")
  Category keyword,
  @Schema(description = "Rank order", example = "1")
  Integer rankOrder,
  @Schema(description = "Product name", example = "Smartphone")
  String name,
  @Schema(description = "Thumbnail URL", example = "https://example.com/image.jpg")
  String thumbnailUrl,
  @Schema(description = "Price", example = "499")
  Integer price,
  @Schema(description = "Detail URL", example = "https://example.com/product/1")
  String detailUrl,
  @Schema(description = "Item ID", example = "12345")
  String itemId,
  @Schema(description = "Registered time", example = "2025-08-11T10:00:00")
  LocalDateTime registeredAt
) {
  public static ProductResponse from(Product p) {
    return new ProductResponse(
        p.getId(),
        p.getKeyword(),
        p.getRankOrder(),
        p.getName(),
        p.getThumbnailUrl(),
        p.getPrice(),
        p.getDetailUrl(),
        p.getItemId(),
        p.getRegisteredAt()
    );
  }
}
