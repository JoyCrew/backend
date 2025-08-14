package com.joycrew.backend.controller;

import com.joycrew.backend.dto.CreateOrderRequest;
import com.joycrew.backend.dto.ErrorResponse;
import com.joycrew.backend.dto.OrderResponse;
import com.joycrew.backend.dto.PagedOrderResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "APIs for purchasing products and tracking orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @Operation(
      summary = "Create an order",
      description = "Creates an order for the current user and deducts points from the linked wallet.",
      responses = {
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
              responseCode = "200",
              description = "Order created successfully.",
              content = @Content(
                  schema = @Schema(implementation = OrderResponse.class),
                  examples = @ExampleObject(
                      value = """
                                            {
                                              "orderId": 1001,
                                              "employeeId": 1,
                                              "productId": 101,
                                              "productName": "Smartphone",
                                              "productItemId": "12345",
                                              "productUnitPrice": 499,
                                              "quantity": 2,
                                              "totalPrice": 998,
                                              "status": "PLACED",
                                              "orderedAt": "2025-08-11T10:00:00",
                                              "shippedAt": null,
                                              "deliveredAt": null
                                            }
                                            """
                  )
              )
          )
      }
  )
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestBody CreateOrderRequest request
  ) {
    Long employeeId = principal.getEmployee().getEmployeeId();
    return ResponseEntity.ok(orderService.createOrder(employeeId, request));
  }

  @Operation(
      summary = "Get my orders (paged)",
      description = "Retrieves the current user's own orders only.",
      parameters = {
          @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
          @Parameter(name = "size", description = "Items per page", example = "20")
      }
  )
  @GetMapping
  public ResponseEntity<PagedOrderResponse> getMyOrders(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Long employeeId = principal.getEmployee().getEmployeeId();
    return ResponseEntity.ok(orderService.getMyOrders(employeeId, page, size));
  }

  @Operation(
      summary = "Get my order detail",
      description = "Retrieves a specific order of the current user."
  )
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getMyOrderDetail(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable Long orderId
  ) {
    Long employeeId = principal.getEmployee().getEmployeeId();
    return ResponseEntity.ok(orderService.getMyOrderDetail(employeeId, orderId));
  }

  @Operation(
      summary = "Cancel my order (only if not shipped)",
      description = "Cancels the current user's order and refunds points if the order has not been shipped yet.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Order canceled successfully.",
              content = @Content(
                  schema = @Schema(implementation = OrderResponse.class),
                  examples = @ExampleObject(
                      value = """
                                            {
                                              "orderId": 1001,
                                              "employeeId": 1,
                                              "productId": 101,
                                              "productName": "Smartphone",
                                              "productItemId": "12345",
                                              "productUnitPrice": 499,
                                              "quantity": 2,
                                              "totalPrice": 998,
                                              "status": "CANCELED",
                                              "orderedAt": "2025-08-11T10:00:00",
                                              "shippedAt": null,
                                              "deliveredAt": null
                                            }
                                            """
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Order cannot be canceled after it has been shipped.",
              content = @Content(
                  schema = @Schema(implementation = ErrorResponse.class),
                  examples = @ExampleObject(
                      value = """
                                            {
                                              "code": "ORDER_CANNOT_CANCEL",
                                              "message": "Order cannot be canceled after it has been shipped.",
                                              "timestamp": "2025-08-11T11:00:00",
                                              "path": "/api/orders/1001/cancel"
                                            }
                                            """
                  )
              )
          )
      }
  )
  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<OrderResponse> cancelMyOrder(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable Long orderId
  ) {
    Long employeeId = principal.getEmployee().getEmployeeId();
    return ResponseEntity.ok(orderService.cancelMyOrder(employeeId, orderId));
  }
}
