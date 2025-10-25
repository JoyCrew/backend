package com.joycrew.backend.service;

import com.joycrew.backend.dto.kakao.ExternalProductDetailResponse;
import com.joycrew.backend.dto.kakao.ExternalProductResponse;
import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.enums.SortOption;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import com.joycrew.backend.repository.KakaoTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // 1. StringUtils import 추가

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalCatalogService {

    private final KakaoTemplateRepository templateRepo;

    @Value("${joycrew.points.krw_per_point:40}")
    private int krwPerPoint;

    // 2. 메소드 시그니처에 searchName 파라미터 추가
    public List<ExternalProductResponse> listByCategory(GiftCategory category, int page, int size, SortOption sort, String searchName) {
        Sort s = switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "basePriceKrw");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "basePriceKrw");
            case POPULAR, NEW -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };

        Pageable pageRequest = PageRequest.of(page, size, s);
        Page<KakaoTemplate> p;

        // 3. searchName (검색어) 유무에 따라 분기 처리
        if (StringUtils.hasText(searchName)) {
            // 검색어가 있는 경우: 이름으로 검색
            // (참고: KakaoTemplateRepository에 이 메소드가 정의되어 있어야 합니다)
            p = templateRepo.findByJoyCategoryAndNameContainingIgnoreCase(category, searchName, pageRequest);
        } else {
            // 검색어가 없는 경우: 기존 로직 (카테고리로만 조회)
            p = templateRepo.findByJoyCategory(category, pageRequest);
        }

        // 4. 조회 결과를 DTO로 변환하여 반환
        return p.getContent().stream().map(t -> {
            int point = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);
            return new ExternalProductResponse(
                    t.getTemplateId(), t.getName(), point, t.getBasePriceKrw(), t.getThumbnailUrl()
            );
        }).toList();
    }

    public ExternalProductDetailResponse getDetailWithPoints(String templateId) {
        var t = templateRepo.findById(templateId).orElse(null);
        if (t == null) return null;

        int basePoint = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);

        return new ExternalProductDetailResponse(
                t.getTemplateId(),
                t.getName(),
                basePoint,
                t.getBasePriceKrw(),
                t.getThumbnailUrl()
        );
    }
}

