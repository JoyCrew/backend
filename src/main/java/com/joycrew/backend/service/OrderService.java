package com.joycrew.backend.service;

import com.joycrew.backend.dto.CreateOrderRequest;
import com.joycrew.backend.dto.OrderResponse;
import com.joycrew.backend.dto.PagedOrderResponse;
import com.joycrew.backend.entity.*;
import com.joycrew.backend.entity.enums.OrderStatus;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.OrderRepository;
import com.joycrew.backend.repository.ProductRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final WalletRepository walletRepository;
    private final EmployeeRepository employeeRepository;

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

        wallet.spendPoints(total);

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
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PagedOrderResponse getMyOrders(Long employeeId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderedAt"));
        Page<Order> p = orderRepository.findByEmployee_EmployeeId(employeeId, pageable);
        return new PagedOrderResponse(
                p.getContent().stream().map(OrderResponse::from).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(),
                p.hasNext(), p.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long employeeId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndEmployee_EmployeeId(orderId, employeeId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
        return OrderResponse.from(order);
    }

    // Cancel my order if not shipped yet
    @Transactional
    public OrderResponse cancelMyOrder(Long employeeId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndEmployee_EmployeeId(orderId, employeeId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));

        // already shipped or delivered cannot cancel
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order cannot be canceled after it has been shipped.");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            // idempotent-ish: 이미 취소된 주문
            return OrderResponse.from(order);
        }

        // refund
        Wallet wallet = order.getEmployee().getWallet();
        wallet.refundPoints(order.getTotalPrice());

        // update status
        order.setStatus(OrderStatus.CANCELED);

        return OrderResponse.from(order);
    }
}
