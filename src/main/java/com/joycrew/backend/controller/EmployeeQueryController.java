package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.EmployeeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/query")
@Tag(name = "Employee Query", description = "API for searching employees")
public class EmployeeQueryController {

    private final EmployeeQueryService employeeQueryService;

    @Operation(
            summary = "Search employee list",
            description = "Performs a unified search by name, email, or department. The current user is excluded from the search results.",
            parameters = {
                    @Parameter(name = "keyword", description = "Search keyword", example = "John"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Items per page", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Employee list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PagedEmployeeResponse.class)
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<PagedEmployeeResponse> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PagedEmployeeResponse response = employeeQueryService.getEmployees(keyword, page, size, principal.getEmployee().getEmployeeId());
        return ResponseEntity.ok(response);
    }
}