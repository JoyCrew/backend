package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "직원 검색 응답 DTO")
public record EmployeeQueryResponse(
        @Schema(example = "1") Long employeeId,
        @Schema(example = "김여은") String employeeName,
        @Schema(example = "kye02@example.com") String email,
        @Schema(example = "사원") String position,
        @Schema(example = "ACTIVE") String status,
        @Schema(example = "EMPLOYEE") UserRole role,
        @Schema(example = "인사팀") String departmentName,
        @Schema(example = "조이크루") String companyName
) {
    public static EmployeeQueryResponse from(Employee e) {
        return EmployeeQueryResponse.builder()
                .employeeId(e.getEmployeeId())
                .employeeName(e.getEmployeeName())
                .email(e.getEmail())
                .position(e.getPosition())
                .status(e.getStatus())
                .role(e.getRole())
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .companyName(e.getCompany().getCompanyName())
                .build();
    }
}
