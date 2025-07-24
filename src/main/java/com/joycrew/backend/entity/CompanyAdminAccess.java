package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.AccessStatus;
import com.joycrew.backend.entity.enums.AdminLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_admin_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminLevel adminLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = true)
    private Employee assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
        if (this.assignedAt == null) {
            this.assignedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = AccessStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}