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
        List<KakaoTemplate> templates = templateRepo.findRandomProducts(10);
        return templates.stream()
                .map(this::convertToExternalProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 없이 이름으로만 전체 상품 검색
     */
    public PagedCatalogResponse searchProductsByName(String searchName, int page, int size, SortOption sort) {
        Sort s = switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "basePriceKrw");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "basePriceKrw");
            case POPULAR, NEW -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };

        Pageable pageRequest = PageRequest.of(page, size, s);
        Page<KakaoTemplate> p = templateRepo.findByNameContainingIgnoreCase(searchName, pageRequest);
        Page<ExternalProductResponse> dtoPage = p.map(this::convertToExternalProductResponse);

        return new PagedCatalogResponse(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isLast()
        );
    }

    /**
     * [CONFLICT FIXED] 카테고리별 상품 조회 (검색 기능 포함)
     * - main 브랜치의 List 반환 대신, PagedCatalogResponse 반환 로직을 채택
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

        // p.map()을 사용해 DTO로 변환
        Page<ExternalProductResponse> dtoPage = p.map(this::convertToExternalProductResponse);

        // PagedCatalogResponse DTO로 감싸서 반환
        return new PagedCatalogResponse(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isLast()
        );
    }

    /**
     * [FIXED] 상품 상세 정보 (brand 필드 추가)
     */
    public ExternalProductDetailResponse getDetailWithPoints(String templateId) {
        var t = templateRepo.findById(templateId).orElse(null);
        if (t == null) return null;

        int basePoint = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);

        return new ExternalProductDetailResponse(
                t.getTemplateId(),
                t.getName(),
                t.getBrand(),
                basePoint,
                t.getBasePriceKrw(),
                t.getThumbnailUrl()
        );
    }

    /**
     * [FIXED] DTO 변환 헬퍼 (brand 필드 추가)
     */
    private ExternalProductResponse convertToExternalProductResponse(KakaoTemplate t) {
        int point = (int) Math.ceil(t.getBasePriceKrw() / (double) krwPerPoint);

        return new ExternalProductResponse(
                t.getTemplateId(),
                t.getName(),
                t.getBrand(), // brand 필드 추가
                point,
                t.getBasePriceKrw(),
                t.getThumbnailUrl()
        );
    }
}