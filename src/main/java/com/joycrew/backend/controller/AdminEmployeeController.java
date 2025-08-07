package com.joycrew.backend.controller;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.AdminEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "직원 관리", description = "HR 관리자의 직원 등록 및 조회 API")
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @Operation(
            summary = "직원 등록",
            description = "HR 관리자가 단일 직원을 등록합니다.",
            security = @SecurityRequirement(name = "Authorization")
    )
    @PostMapping
    public ResponseEntity<EmployeeRegistrationSuccessResponse> registerEmployee(
            @Valid @RequestBody EmployeeRegistrationRequest request
    ) {
        var created = adminEmployeeService.registerEmployee(request);
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
            security = @SecurityRequirement(name = "Authorization")
    )
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> registerEmployeesFromCsv(
            @Parameter(description = "CSV 파일 업로드", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        adminEmployeeService.registerEmployeesFromCsv(file);
        return ResponseEntity.ok("CSV 업로드 및 직원 등록이 완료되었습니다.");
    }

    @Operation(
            summary = "전체 직원 목록 조회 (검색 포함)",
            description = "HR 관리자가 전체 직원 목록을 조회하거나 이름, 이메일, 부서명 기준으로 검색할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "직원 목록 조회 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminPagedEmployeeResponse.class),
                    examples = @ExampleObject(value = """
            {
              "employees": [
                {
                  "employeeId": 1,
                  "employeeName": "김여은",
                  "email": "kye02@example.com",
                  "departmentName": "인사팀",
                  "position": "사원",
                  "profileImageUrl": "https://cdn.joycrew.com/profile/1.jpg",
                  "adminLevel": "EMPLOYEE"
                }
              ],
              "currentPage": 1,
              "totalPages": 1,
              "last": true
            }
        """)
            ))
    })
    @GetMapping
    public ResponseEntity<AdminPagedEmployeeResponse> searchEmployees(
            @Parameter(description = "이름, 이메일, 부서명 중 일부로 검색") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 직원 수", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        AdminPagedEmployeeResponse result = adminEmployeeService.searchEmployees(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "직원 정보 업데이트", security = @SecurityRequirement(name = "Authorization"))
    @PatchMapping("/{employeeId}")
    public ResponseEntity<SuccessResponse> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody AdminEmployeeUpdateRequest request) {
        adminEmployeeService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(new SuccessResponse("직원 정보가 성공적으로 업데이트되었습니다."));
    }

    @Operation(summary = "직원 삭제 (비활성화)", security = @SecurityRequirement(name = "Authorization"))
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<SuccessResponse> deleteEmployee(@PathVariable Long employeeId) {
        adminEmployeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok(new SuccessResponse("직원이 성공적으로 삭제(비활성화) 처리되었습니다."));
    }

    @Operation(summary = "포인트 일괄 분배 및 회수", description = "포인트에 양수 값을 넣으면 분배, 음수 값을 넣으면 회수(반대 거래)됩니다.", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/points/distribute")
    public ResponseEntity<SuccessResponse> distributePoints(
            @Valid @RequestBody AdminPointDistributionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminEmployeeService.distributePoints(request, principal.getEmployee());
        return ResponseEntity.ok(new SuccessResponse("포인트 분배(회수) 작업이 완료되었습니다."));
    }
}
