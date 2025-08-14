package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Product;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paged product response")
public record PagedProductResponse(
  @ArraySchema(arraySchema = @Schema(description = "Content list"),
      schema = @Schema(implementation = ProductResponse.class))
  List<ProductResponse> content,
  @Schema(description = "Current page (0-based)", example = "0")
  int page,
  @Schema(description = "Page size", example = "20")
  int size,
  @Schema(description = "Total elements", example = "123")
  long totalElements,
  @Schema(description = "Total pages", example = "7")
  int totalPages,
  @Schema(description = "Has next page", example = "true")
  boolean hasNext,
  @Schema(description = "Has previous page", example = "false")
  boolean hasPrevious
) {
  public static PagedProductResponse from(org.springframework.data.domain.Page<Product> pageData) {
    List<ProductResponse> mapped = pageData.getContent().stream()
        .map(ProductResponse::from)
        .toList();
    return new PagedProductResponse(
        mapped,
        pageData.getNumber(),
        pageData.getSize(),
        pageData.getTotalElements(),
        pageData.getTotalPages(),
        pageData.hasNext(),
        pageData.hasPrevious()
    );
  }
}
