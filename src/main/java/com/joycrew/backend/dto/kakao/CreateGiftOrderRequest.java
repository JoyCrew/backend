package com.joycrew.backend.dto.kakao;

public record CreateGiftOrderRequest(
        String externalProductId,  // = templateId
        Integer quantity,
        String receiverPhone
) {}
