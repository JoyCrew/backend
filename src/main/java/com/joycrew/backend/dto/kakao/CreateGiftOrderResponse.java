package com.joycrew.backend.dto.kakao;

public record CreateGiftOrderResponse(
        String orderId,
        String status,
        String redeemUrl
) {}
