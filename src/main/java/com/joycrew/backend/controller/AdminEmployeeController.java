package com.joycrew.backend.controller;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.AdminPointService;
import com.joycrew.backend.service.EmployeeManagementService;
import com.joycrew.backend.service.EmployeeRegistrationService;
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

@Tag(name = "Employee Administration", description = "APIs for HR administrators to manage employees.")
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final EmployeeRegistrationService registrationService;
    private final EmployeeManagementService managementService;
    private final AdminPointService pointService;

    @Operation(
            summary = "Register a single employee",
            description = "An HR administrator registers a single employee.",
            security = @SecurityRequirement(name = "Authorization")
    )
    @PostMapping
    public ResponseEntity<EmployeeRegistrationSuccessResponse> registerEmployee(
            @Valid @RequestBody EmployeeRegistrationRequest request
    ) {
        var created = registrationService.registerEmployee(request);
        return ResponseEntity.ok(
                new EmployeeRegistrationSuccessResponse("Employee created successfully (ID: " + created.getEmployeeId() + ")")
        );
    }

    @Operation(
            summary = "Bulk register employees via CSV",
            description = """
                An HR administrator uploads a CSV file to register multiple employees.
                The CSV must include the following headers:
                name,email,initialPassword,companyName,departmentName,position,role,birthday,address,hireDate
            """,
            security = @SecurityRequirement(name = "Authorization")
    )
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> registerEmployeesFromCsv(
            @Parameter(description = "CSV file for upload", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        registrationService.registerEmployeesFromCsv(file);
        return ResponseEntity.ok(new SuccessResponse("CSV processed and employee registration completed."));
    }

    @Operation(
            summary = "Search all employees (with filtering)",
            description = "An HR administrator can search the employee list by name, email, or department."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee list retrieved successfully.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminPagedEmployeeResponse.class),
                    examples = @ExampleObject(value = """
            {
              "employees": [
                {
                  "employeeId": 1,
                  "employeeName": "Jane Doe",
                  "email": "jane.doe@example.com",
                  "departmentName": "HR",
                  "position": "Specialist",
                  "profileImageUrl": "https://cdn.joycrew.com/profile/1.jpg",
                  "adminLevel": "EMPLOYEE",
                  "birthday": "1995-05-10",
                  "address": "123 Teheran-ro, Gangnam-gu, Seoul",
                  "hireDate": "2023-01-10"
                }
              ],
              "currentPage": 0,
              "totalPages": 1,
              "last": true
            }
        """)
            ))
    })
    @GetMapping
    public ResponseEntity<AdminPagedEmployeeResponse> searchEmployees(
            @Parameter(description = "Search keyword for name, email, or department") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of employees per page", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        AdminPagedEmployeeResponse result = managementService.searchEmployees(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update employee information", security = @SecurityRequirement(name = "Authorization"))
    @PatchMapping("/{employeeId}")
    public ResponseEntity<SuccessResponse> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody AdminEmployeeUpdateRequest request) {
        managementService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(new SuccessResponse("Employee information updated successfully."));
    }

    @Operation(summary = "Deactivate an employee (soft delete)", security = @SecurityRequirement(name = "Authorization"))
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<SuccessResponse> deleteEmployee(@PathVariable Long employeeId) {
        managementService.deactivateEmployee(employeeId);
        return ResponseEntity.ok(new SuccessResponse("Employee successfully deactivated."));
    }

    @Operation(summary = "Distribute or revoke points in bulk", description = "Use a positive value for 'points' to distribute, and a negative value to revoke.", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/points/distribute")
    public ResponseEntity<SuccessResponse> distributePoints(
            @Valid @RequestBody AdminPointDistributionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        pointService.distributePoints(request, principal.getEmployee());
        return ResponseEntity.ok(new SuccessResponse("Point distribution process completed successfully."));
    }
}