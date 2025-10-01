package com.joycrew.backend.dto.kakao;

public record CreateGiftOrderRequest(
        String externalProductId,  // = templateId
        String itemId,
        Integer quantity,
        String receiverPhone
) {}
