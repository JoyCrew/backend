package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "상품의 고유 ID", example = "1")
    private Long id;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Schema(description = "상품의 카테고리", example = "BEAUTY")
    private Category keyword;

    @Column(nullable = false)
    @Schema(description = "상품 순위", example = "1")
    private Integer rankOrder;

    @Column(nullable = false, length = 1000)
    @Schema(description = "상품명", example = "Smartphone")
    private String name;

    @Column(nullable = true, length = 2000)
    @Schema(description = "상품 썸네일 URL", example = "https://example.com/image.jpg")
    private String thumbnailUrl;

    @Column(nullable = false)
    @Schema(description = "상품 가격", example = "499")
    private Integer price;

    @Column(nullable = false, length = 2000)
    @Schema(description = "상품 상세 URL", example = "https://example.com/product/1")
    private String detailUrl;

    @Column(nullable = false, length = 64)
    @Schema(description = "상품 고유 아이템 ID", example = "12345")
    private String itemId;

    @Column(nullable = false)
    @Schema(description = "상품 등록 시간", example = "2025-08-11T10:00:00")
    private LocalDateTime registeredAt;
}
