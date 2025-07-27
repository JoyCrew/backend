package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "직원 생성 요청 DTO")
public record CreateEmployeeRequest(
        @Schema(example = "1", description = "회사 ID") Long companyId,
        @Schema(example = "1", description = "부서 ID") Long departmentId,

        @NotBlank
        @Schema(example = "김여은", description = "직원 이름")
        String employeeName,

        @NotBlank
        @Email
        @Schema(example = "kye02@example.com", description = "직장 이메일")
        String email,

        @NotBlank
        @Schema(example = "1234", description = "비밀번호 (raw)")
        String rawPassword,

        @Schema(example = "사원", description = "직급") String position,
        @Schema(example = "https://example.com/profile.png", description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(example = "kye02@naver.com", description = "개인 이메일") String personalEmail,
        @Schema(example = "010-1234-5678", description = "휴대폰 번호") String phoneNumber,
        @Schema(example = "서울시 강남구", description = "배송 주소") String shippingAddress,
        @Schema(example = "true", description = "이메일 알림 수신 여부") Boolean emailNotificationEnabled,
        @Schema(example = "true", description = "앱 알림 수신 여부") Boolean appNotificationEnabled,
        @Schema(example = "ko", description = "언어") String language,
        @Schema(example = "Asia/Seoul", description = "시간대") String timezone,
        @Schema(example = "EMPLOYEE", description = "역할") UserRole role
) {}
