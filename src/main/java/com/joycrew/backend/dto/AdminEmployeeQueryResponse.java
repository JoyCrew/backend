package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "관리자용 직원 검색 응답 DTO")
public record AdminEmployeeQueryResponse(
        @Schema(description = "직원 ID", example = "1")
        Long employeeId,

        @Schema(description = "직원 이름", example = "김조이")
        String employeeName,

        @Schema(description = "직원 이메일", example = "joy@example.com")
        String email,

        @Schema(description = "부서명", example = "개발팀")
        String departmentName,

        @Schema(description = "직책", example = "백엔드 개발자")
        String position,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "직원 권한 등급", example = "HR_ADMIN")
                String adminLevel,

        @Schema(description = "생년월일", example = "1995-05-10")
        LocalDate birthday,

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        String address,

        @Schema(description = "입사일", example = "2023-01-10")
        LocalDate hireDate
) {
    public static AdminEmployeeQueryResponse from(Employee employee) {
        return new AdminEmployeeQueryResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getPosition(),
                employee.getProfileImageUrl(),
                employee.getRole().name(),
                employee.getBirthday(),
                employee.getAddress(),
                employee.getHireDate()
        );
    }
}
