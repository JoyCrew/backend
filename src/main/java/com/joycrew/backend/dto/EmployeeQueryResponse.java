package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직원 검색 결과 응답 DTO")
public record EmployeeQueryResponse(
        @Schema(description = "직원 고유 ID", example = "1")
        Long employeeId,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.joycrew.com/profile/user123.jpg")
        String profileImageUrl,

        @Schema(description = "직원 이름", example = "김조이")
        String employeeName,

        @Schema(description = "부서명", example = "개발팀")
        String departmentName,

        @Schema(description = "직책", example = "백엔드 개발자")
        String position
) {
    public static EmployeeQueryResponse from(Employee employee) {
        String departmentName = (employee.getDepartment() != null) ? employee.getDepartment().getName() : null;

        return new EmployeeQueryResponse(
                employee.getEmployeeId(),
                employee.getProfileImageUrl(),
                employee.getEmployeeName(),
                departmentName,
                employee.getPosition()
        );
    }
}