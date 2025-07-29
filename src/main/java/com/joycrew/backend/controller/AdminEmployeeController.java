package com.joycrew.backend.controller;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.EmployeeRegistrationSuccessResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.service.AdminEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(
            summary = "직원 일괄 등록 (CSV)",
            description = """
        HR 관리자가 CSV 파일을 업로드하여 여러 직원을 등록합니다. 
        CSV는 다음의 헤더를 포함해야 합니다: 
        name,email,initialPassword,companyName,departmentName,position,role
        """,
            security = @SecurityRequirement(name = "Authorization"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "직원 일괄 등록 완료",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = "\"CSV 업로드 및 직원 등록이 완료되었습니다.\""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 형식 또는 CSV 파싱 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "실패 응답 예시",
                                            value = "\"CSV 파일 읽기 실패\""
                                    )
                            )
                    )
            }
    )
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> registerEmployeesFromCsv(
            @Parameter(description = "CSV 파일 업로드", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        adminEmployeeService.registerEmployeesFromCsv(file);
        return ResponseEntity.ok("CSV 업로드 및 직원 등록이 완료되었습니다.");
    }
}
