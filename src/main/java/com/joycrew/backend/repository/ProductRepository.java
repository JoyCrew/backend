package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Product;
import com.joycrew.backend.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByKeyword(Category keyword, Pageable pageable);
}
