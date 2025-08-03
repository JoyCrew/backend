package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이징 처리된 직원 목록 응답 DTO")
public record PagedEmployeeResponse(
        @Schema(description = "직원 정보 목록")
        List<EmployeeQueryResponse> employees,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "10")
        int totalPages,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean isLastPage
) {}