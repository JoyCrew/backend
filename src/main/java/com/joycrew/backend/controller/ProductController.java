package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PagedProductResponse;
import com.joycrew.backend.dto.ProductResponse;
import com.joycrew.backend.entity.enums.Category;
import com.joycrew.backend.service.ProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "APIs for querying products")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductQueryService productQueryService;

  @Operation(
          summary = "Search products with filters (paged)",
          description = "Search products by a query term (name or item ID). You can also filter by category. If the query is empty, it lists products from the specified category or all products if no category is given.",
          parameters = {
                  @Parameter(name = "q", description = "Search query (optional)", example = "Keyboard"),
                  @Parameter(name = "category", description = "Product category to filter by (optional)", example = "APPLIANCES"),
                  @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                  @Parameter(name = "size", description = "Items per page", example = "20")
          }
  )
  @GetMapping("/search")
  public ResponseEntity<PagedProductResponse> searchProducts(
          @RequestParam(name = "q", required = false) String q,
          @RequestParam(name = "category", required = false) Category category,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(productQueryService.searchProducts(q, category, page, size));
  }

  @Operation(
      summary = "Get all products (paged)",
      description = "Fetches all products with pagination.",
      parameters = {
          @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
          @Parameter(name = "size", description = "Items per page", example = "20")
      }
  )
  @GetMapping
  public ResponseEntity<PagedProductResponse> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(productQueryService.getAllProducts(page, size));
  }

  @Operation(
      summary = "Get product by ID",
      description = "Fetches a single product by its ID."
  )
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> getProductById(
      @Parameter(description = "Product ID", example = "1")
      @PathVariable Long id
  ) {
    ProductResponse product = productQueryService.getProductById(id);
    return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
  }

  @Operation(
      summary = "Get products by category (paged)",
      description = "Fetches products by category (keyword) with pagination.",
      parameters = {
          @Parameter(name = "category", description = "Product category (keyword)", example = "BEAUTY"),
          @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
          @Parameter(name = "size", description = "Items per page", example = "20")
      }
  )
  @GetMapping("/category/{category}")
  public ResponseEntity<PagedProductResponse> getProductsByCategory(
      @PathVariable Category category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(productQueryService.getProductsByCategory(category, page, size));
  }
}
