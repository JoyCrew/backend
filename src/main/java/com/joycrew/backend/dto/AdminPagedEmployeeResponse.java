package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자용 직원 목록 페이징 응답 DTO")
public record AdminPagedEmployeeResponse(
        @Schema(description = "직원 목록")
        List<AdminEmployeeQueryResponse> employees,

        @Schema(description = "현재 페이지 번호")
        int currentPage,

        @Schema(description = "전체 페이지 수")
        int totalPages,

        @Schema(description = "마지막 페이지 여부")
        boolean last
) {}
