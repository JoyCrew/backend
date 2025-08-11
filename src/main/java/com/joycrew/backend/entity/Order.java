package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_employee", columnList = "employee_id"),
        @Index(name = "idx_orders_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Product snapshot at the time of order
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 1000)
    private String productName;

    @Column(nullable = false, length = 64)
    private String productItemId;

    @Column(nullable = false)
    private Integer productUnitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        if (this.orderedAt == null) this.orderedAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.PLACED;
    }
}
