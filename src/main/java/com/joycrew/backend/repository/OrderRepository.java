package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Page<Order> findByEmployee_EmployeeId(Long employeeId, Pageable pageable);

  Optional<Order> findByIdAndEmployee_EmployeeId(Long orderId, Long employeeId);
}
