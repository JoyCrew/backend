package com.joycrew.backend.repository;

import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoTemplateRepository extends JpaRepository<KakaoTemplate, String> {
    Page<KakaoTemplate> findByJoyCategory(GiftCategory category, Pageable pageable);
}
