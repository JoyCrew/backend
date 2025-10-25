package com.joycrew.backend.dto.kyc;

import java.util.List;

/**
 * 핸드폰 인증 완료 시 최종 응답 DTO
 */
public record PhoneVerifyResponse(
        boolean verified,
        String kycToken,
        List<VerifiedEmailInfo> accounts // 기존 List<String> emails, String recentLoginAt -> List<VerifiedEmailInfo> accounts
) {}