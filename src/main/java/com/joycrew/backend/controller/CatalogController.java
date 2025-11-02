package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kakao.ExternalProductDetailResponse;
import com.joycrew.backend.dto.kakao.ExternalProductResponse;
import com.joycrew.backend.dto.kakao.PagedCatalogResponse;
import com.joycrew.backend.entity.enums.GiftCategory;
import com.joycrew.backend.entity.enums.SortOption;
import com.joycrew.backend.service.ExternalCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog")
public class CatalogController {

    // (참고: 사장님 코드에서 변수명이 catalog 였는데, catalogService로 바꿨습니다.)
    private final ExternalCatalogService catalogService;

    /**
     * 추천 상품 (랜덤) - 이건 List가 맞음 (수정 X)
     */
    @GetMapping(value = "/featured", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<ExternalProductResponse>> getFeaturedProducts() {
        return ResponseEntity.ok(catalogService.getFeaturedProducts());
    }

    /**
     * [수정] 카테고리 없이 이름으로만 전체 상품 검색
     * (반환 타입을 List -> PagedCatalogResponse 로 변경)
     */
    @GetMapping(value = "/search", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedCatalogResponse> searchProducts( // [수정] 반환 타입
                                                                @RequestParam String searchName,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(defaultValue = "POPULAR") SortOption sort
    ) {
        PagedCatalogResponse response = catalogService.searchProductsByName(searchName, page, size, sort); // [수정]
        return ResponseEntity.ok(response);
    }

    /**
     * [수정] 카테고리별 상품 조회 (검색 포함)
     * (반환 타입을 List -> PagedCatalogResponse 로 변경)
     */
    @GetMapping(value = "/kakao/{category}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedCatalogResponse> listKakaoByCategory( // [수정] 반환 타입
                                                                     @PathVariable String category,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     @RequestParam(defaultValue = "POPULAR") SortOption sort,
                                                                     @RequestParam(required = false) String searchName
    ) {
        GiftCategory gc;
        try {
            // (참고: .trim()을 추가하여 " CAFE " 같은 공백 포함 입력도 처리)
            gc = GiftCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 잘못된 카테고리명
        }

        PagedCatalogResponse response = catalogService.listByCategory(gc, page, size, sort, searchName); // [수정]
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 상세 정보 조회 (수정 X)
     * (참고: 사장님 코드에서 메서드명이 detailPoints 였는데 getProductDetail로 바꿨습니다.)
     */
    @GetMapping(value = "/kakao/product/{templateId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<ExternalProductDetailResponse> getProductDetail(
            @PathVariable String templateId
    ) {
        var resp = catalogService.getDetailWithPoints(templateId);
        return resp == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resp);
    }
}