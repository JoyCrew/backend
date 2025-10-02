package com.joycrew.backend.dto.kakao;

import java.util.Map;

/** 응답 스키마는 문서 예시에 맞추어 맵으로 유연 처리(필요시 필드 확정) */
public record KakaoTemplateOrderResponse(
        Map<String, Object> data
) {}
