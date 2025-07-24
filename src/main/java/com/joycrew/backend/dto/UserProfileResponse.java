package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 응답 DTO")
public class UserProfileResponse {

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long employeeId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "현재 총 포인트 잔액", example = "1200")
    private Integer totalBalance;

    @Schema(description = "현재 선물 가능한 포인트 잔액", example = "50")
    private Integer giftableBalance;

    @Schema(description = "사용자 역할", example = "EMPLOYEE", allowableValues = {"EMPLOYEE", "MANAGER", "HR_ADMIN", "SUPER_ADMIN"})
    private UserRole role;

    @Schema(description = "소속 부서", example = "개발팀", nullable = true)
    private String department;

    @Schema(description = "직책", example = "대리", nullable = true)
    private String position;
}