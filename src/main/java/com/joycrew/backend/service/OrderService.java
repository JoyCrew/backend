package com.joycrew.backend.service;

import com.joycrew.backend.dto.CreateOrderRequest;
import com.joycrew.backend.dto.OrderResponse;
import com.joycrew.backend.dto.PagedOrderResponse;
import com.joycrew.backend.entity.*;
import com.joycrew.backend.entity.enums.OrderStatus;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final WalletRepository walletRepository;
  private final EmployeeRepository employeeRepository;
  private final RewardPointTransactionRepository transactionRepository;

  @Transactional
  public OrderResponse createOrder(Long employeeId, CreateOrderRequest req) {
    Product product = productRepository.findById(req.productId())
        .orElseThrow(() -> new NoSuchElementException("Product not found"));

    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new NoSuchElementException("Employee not found"));

    Wallet wallet = walletRepository.findByEmployee_EmployeeId(employeeId)
        .orElseThrow(() -> new NoSuchElementException("Wallet not found"));

    int qty = (req.quantity() == null || req.quantity() <= 0) ? 1 : req.quantity();
    int total = product.getPrice() * qty;

    wallet.purchaseWithPoints(total);

    // Create and save the transaction history
    RewardPointTransaction transaction = RewardPointTransaction.builder()
        .sender(employee)
        .receiver(null) // No specific receiver for item redemption
        .pointAmount(total)
        .message(String.format("Purchased: %s", product.getName()))
        .type(TransactionType.REDEEM_ITEM)
        .build();
    transactionRepository.save(transaction);

    Order order = Order.builder()
        .employee(employee)
        .productId(product.getId())
        .productName(product.getName())
        .productItemId(product.getItemId())
        .productUnitPrice(product.getPrice())
        .quantity(qty)
        .totalPrice(total)
        .status(OrderStatus.PLACED)
        .orderedAt(LocalDateTime.now())
        .build();

    Order saved = orderRepository.save(order);

    return OrderResponse.from(saved, product.getThumbnailUrl());
  }

  @Transactional(readOnly = true)
  public PagedOrderResponse getMyOrders(Long employeeId, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderedAt"));
    Page<Order> orderPage = orderRepository.findByEmployee_EmployeeId(employeeId, pageable);

    List<Long> productIds = orderPage.getContent().stream()
        .map(Order::getProductId)
        .distinct()
        .toList();

    Map<Long, String> productThumbnailMap = productRepository.findAllById(productIds).stream()
        .collect(Collectors.toMap(Product::getId, Product::getThumbnailUrl));

    List<OrderResponse> orderResponses = orderPage.getContent().stream()
        .map(order -> {
          String thumbnailUrl = productThumbnailMap.get(order.getProductId());
          return OrderResponse.from(order, thumbnailUrl);
        })
        .toList();

    return new PagedOrderResponse(
        orderResponses,
        orderPage.getNumber(),
        orderPage.getSize(),
        orderPage.getTotalElements(),
        orderPage.getTotalPages(),
        orderPage.hasNext(),
        orderPage.hasPrevious()
    );
  }

  @Transactional(readOnly = true)
  public OrderResponse getMyOrderDetail(Long employeeId, Long orderId) {
    Order order = orderRepository.findByOrderIdAndEmployee_EmployeeId(orderId, employeeId)
        .orElseThrow(() -> new NoSuchElementException("Order not found"));

    String thumbnailUrl = productRepository.findById(order.getProductId())
        .map(Product::getThumbnailUrl)
        .orElse(null);

    return OrderResponse.from(order, thumbnailUrl);
  }

  @Transactional
  public OrderResponse cancelMyOrder(Long employeeId, Long orderId) {
    Order order = orderRepository.findByOrderIdAndEmployee_EmployeeId(orderId, employeeId)
        .orElseThrow(() -> new NoSuchElementException("Order not found"));

    if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
      throw new IllegalStateException("Order cannot be canceled after it has been shipped.");
    }

    String thumbnailUrl = null;
    if (order.getStatus() != OrderStatus.CANCELED) {
      Wallet wallet = order.getEmployee().getWallet();
      wallet.refundPoints(order.getTotalPrice());
      order.setStatus(OrderStatus.CANCELED);
    }

    thumbnailUrl = productRepository.findById(order.getProductId())
        .map(Product::getThumbnailUrl)
        .orElse(null);

    return OrderResponse.from(order, thumbnailUrl);
  }
}