package com.joycrew.backend.controller;

import com.joycrew.backend.dto.EmployeeQueryResponse;
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
@Tag(name = "직원 조회", description = "직원 목록 검색 API")
public class EmployeeQueryController {

    private final EmployeeQueryService employeeQueryService;

    @Operation(
            summary = "직원 목록 검색",
            description = "이름, 이메일, 부서명을 기준으로 통합 검색을 수행합니다. 검색 결과에서는 본인이 제외됩니다.",
            parameters = {
                    @Parameter(name = "keyword", description = "검색 키워드", example = "김"),
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지당 개수", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "직원 목록 조회 성공",
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