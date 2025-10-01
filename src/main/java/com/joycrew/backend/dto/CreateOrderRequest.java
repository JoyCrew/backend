package com.joycrew.backend.dto;

public record CreateOrderRequest(
        String externalProductId,   // KakaoTemplate.templateId
        Integer quantity            // null 또는 <=0 이면 1
) {}
