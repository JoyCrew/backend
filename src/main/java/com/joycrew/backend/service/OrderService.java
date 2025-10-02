package com.joycrew.backend.service;

import com.joycrew.backend.dto.OrderResponse;
import com.joycrew.backend.dto.PagedOrderResponse;
import com.joycrew.backend.entity.Order;
import com.joycrew.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepository;

  @Transactional(readOnly = true)
  public PagedOrderResponse getMyOrders(Long employeeId, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderedAt"));
    Page<Order> pageData = orderRepository.findByEmployee_EmployeeId(employeeId, pageable);

    List<OrderResponse> items = pageData.getContent().stream()
            .map(o -> OrderResponse.from(o, null))
            .toList();

    return new PagedOrderResponse(
            items,
            pageData.getNumber(),
            pageData.getSize(),
            pageData.getTotalElements(),
            pageData.getTotalPages(),
            pageData.hasNext(),
            pageData.hasPrevious()
    );
  }

  @Transactional(readOnly = true)
  public OrderResponse getMyOrderDetail(Long employeeId, Long orderId) {
    Order order = orderRepository.findByIdAndEmployee_EmployeeId(orderId, employeeId)
            .orElseThrow(() -> new NoSuchElementException("Order not found"));
    return OrderResponse.from(order, null);
  }

}
