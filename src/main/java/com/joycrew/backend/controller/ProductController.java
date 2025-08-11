package com.joycrew.backend.controller;

import com.joycrew.backend.dto.ErrorResponse;
import com.joycrew.backend.dto.PagedProductResponse;
import com.joycrew.backend.dto.ProductResponse;
import com.joycrew.backend.entity.enums.Category;
import com.joycrew.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "APIs for querying products")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get all products (paged)",
            description = "Fetches all products with pagination.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Items per page", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved products.",
                            content = @Content(
                                    schema = @Schema(implementation = PagedProductResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "keyword": "BEAUTY",
                                                  "rankOrder": 1,
                                                  "name": "Smartphone",
                                                  "thumbnailUrl": "https://example.com/image.jpg",
                                                  "price": 499,
                                                  "detailUrl": "https://example.com/product/1",
                                                  "itemId": "12345",
                                                  "registeredAt": "2025-08-11T10:00:00"
                                                }
                                              ],
                                              "page": 0,
                                              "size": 20,
                                              "totalElements": 123,
                                              "totalPages": 7,
                                              "hasNext": true,
                                              "hasPrevious": false
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<PagedProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @Operation(
            summary = "Get product by ID",
            description = "Fetches a single product by its ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the product.",
                            content = @Content(
                                    schema = @Schema(implementation = ProductResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "id": 1,
                                              "keyword": "BEAUTY",
                                              "rankOrder": 1,
                                              "name": "Smartphone",
                                              "thumbnailUrl": "https://example.com/image.jpg",
                                              "price": 499,
                                              "detailUrl": "https://example.com/product/1",
                                              "itemId": "12345",
                                              "registeredAt": "2025-08-11T10:00:00"
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content // empty body for 404
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id
    ) {
        ProductResponse product = productService.getProductById(id);
        return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Get products by category (paged)",
            description = "Fetches products by category (keyword) with pagination.",
            parameters = {
                    @Parameter(name = "category", description = "Product category (keyword)", example = "BEAUTY"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Items per page", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved products by category.",
                            content = @Content(
                                    schema = @Schema(implementation = PagedProductResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "keyword": "BEAUTY",
                                                  "rankOrder": 1,
                                                  "name": "Smartphone",
                                                  "thumbnailUrl": "https://example.com/image.jpg",
                                                  "price": 499,
                                                  "detailUrl": "https://example.com/product/1",
                                                  "itemId": "12345",
                                                  "registeredAt": "2025-08-11T10:00:00"
                                                }
                                              ],
                                              "page": 0,
                                              "size": 20,
                                              "totalElements": 45,
                                              "totalPages": 3,
                                              "hasNext": true,
                                              "hasPrevious": false
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid category",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "code": "INVALID_CATEGORY",
                                              "message": "Category value is invalid.",
                                              "timestamp": "2025-08-11T10:45:00",
                                              "path": "/api/products/category/FOO"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/category/{category}")
    public ResponseEntity<PagedProductResponse> getProductsByCategory(
            @PathVariable Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.getProductsByCategory(category, page, size));
    }
}
