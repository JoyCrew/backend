package com.joycrew.backend.service;

import com.joycrew.backend.dto.PagedProductResponse;
import com.joycrew.backend.dto.ProductResponse;
import com.joycrew.backend.entity.Product;
import com.joycrew.backend.entity.enums.Category;
import com.joycrew.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

  private final ProductRepository productRepository;

  // Get all products (paginated)
  public PagedProductResponse getAllProducts(int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    Page<Product> result = productRepository.findAll(pageable);
    return PagedProductResponse.from(result);
  }

  // Get a single product
  public ProductResponse getProductById(Long id) {
    return productRepository.findById(id)
            .map(ProductResponse::from)
            .orElse(null);
  }

  // Get by category (paginated)
  public PagedProductResponse getProductsByCategory(Category category, int page, int size) {
    PageRequest pageable = PageRequest.of(
            page, size,
            Sort.by(Sort.Direction.ASC, "rankOrder").and(Sort.by(Sort.Direction.DESC, "id"))
    );
    Page<Product> result = productRepository.findByKeyword(category, pageable);
    return PagedProductResponse.from(result);
  }

  // Search (fallback to all products if query is empty, with optional category)
  public PagedProductResponse searchProducts(String q, Category category, int page, int size) {
    PageRequest pageable = PageRequest.of(
            page, size,
            Sort.by(Sort.Direction.ASC, "rankOrder").and(Sort.by(Sort.Direction.DESC, "id"))
    );

    // If query is null or empty, behavior depends on category
    if (q == null || q.trim().isEmpty()) {
      return (category == null)
              ? getAllProducts(page, size)
              : getProductsByCategory(category, page, size);
    }

    String keyword = q.trim();
    Page<Product> result = (category == null)
            ? productRepository.searchByQuery(keyword, pageable)
            : productRepository.searchByCategoryAndQuery(category, keyword, pageable);

    return PagedProductResponse.from(result);
  }
}