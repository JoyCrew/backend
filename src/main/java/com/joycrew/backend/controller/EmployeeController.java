package com.joycrew.backend.controller;

import com.joycrew.backend.dto.CreateEmployeeRequest;
import com.joycrew.backend.dto.CreateEmployeeSuccessResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.service.EmployeeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "직원 관리", description = "HR 관리자의 단일 직원 등록 API")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeAdminService employeeAdminService;

    @Operation(
            summary = "직원 생성",
            description = "HR 관리자가 단일 직원을 등록합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "직원 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CreateEmployeeSuccessResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<CreateEmployeeSuccessResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        Employee created = employeeAdminService.createEmployee(request);
        return ResponseEntity.ok(new CreateEmployeeSuccessResponse("직원 생성 완료 (ID: " + created.getEmployeeId() + ")"));
    }
}
