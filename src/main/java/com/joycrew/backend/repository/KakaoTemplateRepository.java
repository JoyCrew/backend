package com.joycrew.backend.repository;

import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface KakaoTemplateRepository extends JpaRepository<KakaoTemplate, String> {

    // Existing method
    Page<KakaoTemplate> findByJoyCategory(GiftCategory category, Pageable pageable);

    // Existing method
    Page<KakaoTemplate> findByJoyCategoryAndNameContainingIgnoreCase(GiftCategory category, String name, Pageable pageable);

    /**
     * [NEW] 카테고리 없이 이름으로만 전체 검색
     */
    Page<KakaoTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * [NEW] 추천 상품용: 랜덤 N개 조회
     */
    @Query(value = "SELECT * FROM kakao_template ORDER BY RAND() LIMIT :limit",
            nativeQuery = true)
    List<KakaoTemplate> findRandomProducts(@Param("limit") int limit);
}