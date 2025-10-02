package com.joycrew.backend.controller;

import com.joycrew.backend.dto.CreateOrderRequest;
import com.joycrew.backend.dto.OrderResponse;
import com.joycrew.backend.dto.PagedOrderResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.GiftPurchaseService;
import com.joycrew.backend.service.OrderService; // 조회용
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "Purchase Kakao gifts with points (no cancellation)")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final GiftPurchaseService giftPurchaseService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateOrderRequest request
    ) {
        Long employeeId = principal.getEmployee().getEmployeeId();
        return ResponseEntity.ok(giftPurchaseService.purchaseWithPoints(employeeId, request));
    }

    @GetMapping
    public ResponseEntity<PagedOrderResponse> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long employeeId = principal.getEmployee().getEmployeeId();
        return ResponseEntity.ok(orderService.getMyOrders(employeeId, page, size));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrderDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId
    ) {
        Long employeeId = principal.getEmployee().getEmployeeId();
        return ResponseEntity.ok(orderService.getMyOrderDetail(employeeId, orderId));
    }
}
