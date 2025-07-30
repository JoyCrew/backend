package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "직원 검색 응답 DTO")
public record EmployeeQueryResponse(
        @Schema(description = "프로필 이미지 URL", example = "https://cdn.joycrew.com/profile/user123.jpg") String profileImageUrl,
        @Schema(description = "직원 이름", example = "김여은") String employeeName,
        @Schema(description = "부서명", example = "인사팀") String departmentName,
        @Schema(description = "직책", example = "사원") String position
) {
    public static EmployeeQueryResponse from(Employee e) {
        return EmployeeQueryResponse.builder()
                .profileImageUrl(e.getProfileImageUrl())
                .employeeName(e.getEmployeeName())
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .position(e.getPosition())
                .build();
    }
}
