package com.joycrew.backend.dto.kakao;

public record ExternalProductResponse(
        String templateId,
        String name,
        String brand,
        int pointPrice,
        int priceKrw,
        String thumbnailUrl
) {}
