package com.joycrew.backend.dto.kakao;

import java.util.List;

/**
 * 카탈로그 API용 페이지 응답 DTO
 * (페이지 정보 + 상품 목록을 포함)
 */
public record PagedCatalogResponse(
        List<ExternalProductResponse> products, // 상품 목록 (기존 content)
        int currentPage,                      // 현재 페이지 (0부터 시작)
        int totalPages,                       // 전체 페이지 수
        long totalElements,                   // 전체 상품 개수
        boolean last                          // 마지막 페이지 여부 (hasNext 대신)
) {}