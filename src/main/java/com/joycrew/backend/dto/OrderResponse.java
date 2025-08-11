package com.joycrew.backend.dto;

import com.joycrew.backend.entity.Order;
import com.joycrew.backend.entity.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order response")
public record OrderResponse(
        @Schema(description = "Order ID", example = "1001")
        Long orderId,
        @Schema(description = "Employee ID", example = "1")
        Long employeeId,
        @Schema(description = "Product ID", example = "101")
        Long productId,
        @Schema(description = "Product name", example = "Smartphone")
        String productName,
        @Schema(description = "Product item ID", example = "12345")
        String productItemId,
        @Schema(description = "Unit price", example = "499")
        Integer productUnitPrice,
        @Schema(description = "Quantity", example = "2")
        Integer quantity,
        @Schema(description = "Total price", example = "998")
        Integer totalPrice,
        @Schema(description = "Status", example = "PLACED")
        OrderStatus status,
        @Schema(description = "Ordered at", example = "2025-08-11T10:00:00")
        LocalDateTime orderedAt,
        @Schema(description = "Shipped at", example = "2025-08-12T09:00:00")
        LocalDateTime shippedAt,
        @Schema(description = "Delivered at", example = "2025-08-13T18:30:00")
        LocalDateTime deliveredAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getOrderId(),
                o.getEmployee().getEmployeeId(),
                o.getProductId(),
                o.getProductName(),
                o.getProductItemId(),
                o.getProductUnitPrice(),
                o.getQuantity(),
                o.getTotalPrice(),
                o.getStatus(),
                o.getOrderedAt(),
                o.getShippedAt(),
                o.getDeliveredAt()
        );
    }
}