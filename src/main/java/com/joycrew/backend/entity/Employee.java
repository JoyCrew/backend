package com.joycrew.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String passwordHash;
    private String employeeName;
    private String email;
    private String position;
    private String status;
    private String role;

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "employee")
    private List<Wallet> wallets;

    @OneToMany(mappedBy = "sender")
    private List<RewardPointTransaction> sentTransactions;

    @OneToMany(mappedBy = "receiver")
    private List<RewardPointTransaction> receivedTransactions;

    @OneToMany(mappedBy = "employee")
    private List<CompanyAdminAccess> adminAccesses;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
