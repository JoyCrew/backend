// src/main/java/com/joycrew/backend/dto/OrderResponse.java
package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Order;
import com.joycrew.backend.entity.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "주문 응답")
@Builder
public record OrderResponse(
        Long orderId,
        String productName,
        Integer quantity,
        Integer unitPoint,
        Integer totalPoint,
        OrderStatus status,
        LocalDateTime orderedAt,
        String externalOrderId,
        String thumbnailUrl
) {
    public static OrderResponse from(Order o, String thumbnailUrl) {
        return OrderResponse.builder()
                .orderId(o.getId())                            // ✅ getOrderId -> getId
                .productName(o.getProductName())
                .quantity(o.getQuantity())
                .unitPoint(o.getProductUnitPrice())
                .totalPoint(o.getTotalPrice())
                .status(o.getStatus())
                .orderedAt(o.getOrderedAt())
                .externalOrderId(o.getExternalOrderId())       // 추가했으면 매핑, 없으면 제거
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
