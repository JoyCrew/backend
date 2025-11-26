package com.joycrew.backend.dto.kakao;

public record ExternalProductDetailResponse(
        String templateId,
        String name,
        String brand,
        int pointPrice,         // 환산 포인트 (ceil(basePriceKrw / krwPerPoint))
        int priceKrw,
        String thumbnailUrl
) {}
