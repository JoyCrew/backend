package com.joycrew.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recent_product_view",
        uniqueConstraints = @UniqueConstraint(name = "uq_employee_product", columnNames = {"employee_id", "product_id"}),
        indexes = {
                @Index(name = "idx_rpv_employee_viewed", columnList = "employee_id, viewedAt"),
                @Index(name = "idx_rpv_viewed", columnList = "viewedAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who viewed
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // which product
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    public void onCreate() {
        if (this.viewedAt == null) this.viewedAt = LocalDateTime.now();
    }
}
