package com.joycrew.backend.dto.kakao;

public record ExternalProductOptionPointDto(
        String itemId,
        String name,
        Integer pricePoint,
        Integer priceKrw
) {}
