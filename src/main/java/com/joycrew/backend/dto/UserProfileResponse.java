package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 프로필 응답 DTO")
public record UserProfileResponse(
        @Schema(description = "사용자 고유 ID") Long employeeId,
        @Schema(description = "사용자 이름") String name,
        @Schema(description = "이메일 주소") String email,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(description = "현재 총 포인트 잔액") Integer totalBalance,
        @Schema(description = "현재 선물 가능한 포인트 잔액") Integer giftableBalance,
        @Schema(description = "사용자 권한")AdminLevel level,
        @Schema(description = "소속 부서") String department,
        @Schema(description = "직책") String position
) {
    public static UserProfileResponse from(Employee employee, Wallet wallet) {
        String departmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
        return new UserProfileResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                employee.getProfileImageUrl(),
                wallet.getBalance(),
                wallet.getGiftablePoint(),
                employee.getRole(),
                departmentName,
                employee.getPosition()
        );
    }
}
