package com.joycrew.backend.repository;

import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoTemplateRepository extends JpaRepository<KakaoTemplate, String> {

    // Existing method
    Page<KakaoTemplate> findByJoyCategory(GiftCategory category, Pageable pageable);

    // [NEW] Added method for searching by name (resolves error in ExternalCatalogService)
    Page<KakaoTemplate> findByJoyCategoryAndNameContainingIgnoreCase(GiftCategory category, String name, Pageable pageable);
}
