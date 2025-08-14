package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Product;
import com.joycrew.backend.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Page<Product> findByKeyword(Category keyword, Pageable pageable);

  @Query("""
        SELECT p
        FROM Product p
        WHERE (:q IS NULL OR :q = '')
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR p.itemId      LIKE CONCAT('%', :q, '%')
        """)
  Page<Product> searchByQuery(@Param("q") String q, Pageable pageable);

  @Query("""
        SELECT p
        FROM Product p
        WHERE p.keyword = :category
          AND (
                :q IS NULL OR :q = ''
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.itemId      LIKE CONCAT('%', :q, '%')
              )
        """)
  Page<Product> searchByCategoryAndQuery(@Param("category") Category category,
                                         @Param("q") String q,
                                         Pageable pageable);
}
