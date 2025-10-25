package com.joycrew.backend.dto.kyc;

import java.time.LocalDateTime;

/**
 * KYC(본인인증) 완료 시 반환하는 계정(이메일)과 최근 로그인 날짜 DTO
 */
public record VerifiedEmailInfo(
        String email,
        LocalDateTime recentLoginAt // JSON 직렬화 시 ISO-8601 날짜 문자열로 자동 변환
) {}
