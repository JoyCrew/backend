package com.joycrew.backend.service;

import com.joycrew.backend.dto.kakao.ExternalProductDetailResponse;
import com.joycrew.backend.dto.kakao.ExternalProductResponse;
import com.joycrew.backend.dto.kakao.PagedCatalogResponse;
import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.enums.SortOption;
import com.joycrew.backend.entity.kakao.KakaoTemplate;
import com.joycrew.backend.repository.KakaoTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalCatalogService {

    private final KakaoTemplateRepository templateRepo;

    @Value("${joycrew.points.krw_per_point:40}")
    private int krwPerPoint;

    /**
     * 추천 상품 (랜덤) - 이건 페이지 정보가 필요 없으므로 List 유지
     */
    public List<ExternalProductResponse> getFeaturedProducts() {
        // (레포지토리에 findRandomProducts(10)이 정의되어 있어야 함)
        List<KakaoTemplate> templates = templateRepo.findRandomProducts(10);
        return templates.stream()
                .map(this::convertToExternalProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * [수정] 카테고리 없이 이름으로만 전체 상품 검색
     * (반환 타입을 List -> PagedCatalogResponse 로 변경)
     */
    public PagedCatalogResponse searchProductsByName(String searchName, int page, int size, SortOption sort) {
        Sort s = switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "basePriceKrw");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "basePriceKrw");
            case POPULAR, NEW -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };

        Pageable pageRequest = PageRequest.of(page, size, s);

        // (레포지토리에 findByNameContainingIgnoreCase(name, pageable)이 정의되어 있어야 함)
        Page<KakaoTemplate> p = templateRepo.findByNameContainingIgnoreCase(searchName, pageRequest);

        // [수정] p.getContent() 대신 p.map()을 사용해 DTO로 변환
        Page<ExternalProductResponse> dtoPage = p.map(this::convertToExternalProductResponse);

        // [수정] 새로 만든 PagedCatalogResponse DTO로 감싸서 반환
        return new PagedCatalogResponse(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isLast()
        );
    }

    /**
     * [수정] 카테고리별 상품 조회 (검색 기능 포함)
     * (반환 타입을 List -> PagedCatalogResponse 로 변경)
     */
    public PagedCatalogResponse listByCategory(GiftCategory category, int page, int size, SortOption sort, String searchName) {
        Sort s = switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "basePriceKrw");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "basePriceKrw");
            case POPULAR, NEW -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };

        Pageable pageRequest = PageRequest.of(page, size, s);
        Page<KakaoTemplate> p;

        if (StringUtils.hasText(searchName)) {
            p = templateRepo.findByJoyCategoryAndNameContainingIgnoreCase(category, searchName, pageRequest);
        } else {
            p = templateRepo.findByJoyCategory(category, pageRequest);
        }

        // [수정] p.map()을 사용해 DTO로 변환
        Page<ExternalProductResponse> dtoPage = p.map(this::convertToExternalProductResponse);

        // [수정] 새로 만든 PagedCatalogResponse DTO로 감싸서 반환
        return new PagedCatalogResponse(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isLast()
        );
    }

    /**
     * 상품 상세 정보 (이건 단건 조회라 수정 필요 없음)
     */
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

    /**
     * DTO 변환 헬퍼 (공통 로직)
     */
    private ExternalProductResponse convertToExternalProductResponse(KakaoTemplate t) {
        int point = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);

        return new ExternalProductResponse(
                t.getTemplateId(),
                t.getName(),
                point,
                t.getBasePriceKrw(),
                t.getThumbnailUrl()
        );
    }
}