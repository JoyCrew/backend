package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@Schema(description = "사용자 프로필 수정 요청 DTO")
public class UserProfileUpdateRequest {

    @Schema(description = "새로운 사용자 이름 (선호하는 이름)", example = "김조이", nullable = true)
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요.")
    private String name;

    @Schema(description = "새로운 비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "newPassword123!", nullable = true)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상 20자 이하이어야 합니다.")
    private String password;

    @Schema(description = "프로필 사진 이미지 URL", example = "https://example.com/profile.jpg", nullable = true)
    @URL(message = "유효한 URL 형식이 아닙니다.")
    private String profileImageUrl;

    @Schema(description = "개인 이메일 주소", example = "joy@personal.com", nullable = true)
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String personalEmail;

    @Schema(description = "휴대폰 번호", example = "010-1234-5678", nullable = true)
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효한 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
    private String phoneNumber;

    @Schema(description = "배송 주소 (리워드 배송 시 필요)", example = "서울시 강남구 테헤란로 123", nullable = true)
    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다.")
    private String shippingAddress;

    @Schema(description = "이메일 알림 수신 여부", example = "true", nullable = true)
    private Boolean emailNotificationEnabled;

    @Schema(description = "앱 내 알림 수신 여부", example = "true", nullable = true)
    private Boolean appNotificationEnabled;

    @Schema(description = "선호 언어 설정", example = "ko-KR", nullable = true)
    private String language;

    @Schema(description = "시간대 설정", example = "Asia/Seoul", nullable = true)
    private String timezone;
}
