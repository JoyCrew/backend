package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated Employee List Response DTO")
public record PagedEmployeeResponse(
        @Schema(description = "List of employee information")
        List<EmployeeQueryResponse> employees,

        @Schema(description = "Current page number (0-based)", example = "0")
        int currentPage,

        @Schema(description = "Total number of pages", example = "10")
        int totalPages,

        @Schema(description = "Indicates if this is the last page", example = "false")
        boolean isLastPage
) {}