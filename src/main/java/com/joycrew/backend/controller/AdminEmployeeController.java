package com.joycrew.backend.controller;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.EmployeeRegistrationSuccessResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.service.AdminEmployeeService;
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
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @Operation(
            summary = "직원 등록",
            description = "HR 관리자가 단일 직원을 등록합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "직원 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeRegistrationSuccessResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<EmployeeRegistrationSuccessResponse> registerEmployee(
            @Valid @RequestBody EmployeeRegistrationRequest request
    ) {
        Employee created = adminEmployeeService.registerEmployee(request);
        return ResponseEntity.ok(
                new EmployeeRegistrationSuccessResponse("직원 생성 완료 (ID: " + created.getEmployeeId() + ")")
        );
    }
}
