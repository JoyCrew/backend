package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kakao.ExternalProductDetailResponse;
import com.joycrew.backend.dto.kakao.ExternalProductResponse;
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

    private final ExternalCatalogService catalog;

    @GetMapping(value = "/kakao/{category}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<ExternalProductResponse>> listKakaoByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "POPULAR") SortOption sort,
            @RequestParam(required = false) String searchName // 상품명 검색 파라미터 추가
    ) {
        GiftCategory gc = GiftCategory.valueOf(category.toUpperCase());

        // 서비스 레이어에도 searchName 파라미터 전달 (ExternalCatalogService.listByCategory 수정 필요)
        return ResponseEntity.ok(catalog.listByCategory(gc, page, size, sort, searchName));
    }

    @GetMapping(value = "/kakao/product/{templateId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<ExternalProductDetailResponse> detailPoints(@PathVariable String templateId) {
        var resp = catalog.getDetailWithPoints(templateId);
        return resp == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resp);
    }
}