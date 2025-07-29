package com.joycrew.backend.controller;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.entity.enums.EmployeeQueryType;
import com.joycrew.backend.service.EmployeeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeQueryController {

    private final EmployeeQueryService employeeQueryService;

    @Operation(
            summary = "직원 목록 조회 및 검색",
            description = "검색 유형(type: NAME, EMAIL, DEPARTMENT)과 키워드를 기준으로 직원 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "type", description = "검색 기준 (NAME, EMAIL, DEPARTMENT)", example = "NAME"),
                    @Parameter(name = "keyword", description = "검색 키워드", example = "김"),
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지당 개수", example = "10")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "직원 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = EmployeeQueryResponse.class))
                            )
                    )
            }
    )
    @GetMapping
    public List<EmployeeQueryResponse> searchEmployees(
            @RequestParam(defaultValue = "NAME") EmployeeQueryType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return employeeQueryService.getEmployees(type, keyword, page, size);
    }
}
