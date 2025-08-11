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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Get all products (paged)
    public PagedProductResponse getAllProducts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> result = productRepository.findAll(pageable);
        return PagedProductResponse.from(result);
    }

    // Get product by ID (single)
    public ProductResponse getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ProductResponse::from).orElse(null);
    }

    // Get products by category (paged)
    public PagedProductResponse getProductsByCategory(Category category, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rankOrder").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Product> result = productRepository.findByKeyword(category, pageable);
        return PagedProductResponse.from(result);
    }
}
