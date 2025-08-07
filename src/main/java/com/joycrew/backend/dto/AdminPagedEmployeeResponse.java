package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Admin-specific Paginated Employee List Response DTO")
public record AdminPagedEmployeeResponse(
        @Schema(description = "List of employees")
        List<AdminEmployeeQueryResponse> employees,

        @Schema(description = "Current page number (0-based)")
        int currentPage,

        @Schema(description = "Total number of pages")
        int totalPages,

        @Schema(description = "Indicates if this is the last page")
        boolean last
) {}