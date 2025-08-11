package com.joycrew.backend.entity;

import com.joycrew.backend.exception.InsufficientPointsException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    private String companyName;
    private String status;
    private LocalDateTime startAt;

    @Column(nullable = false)
    private Double totalCompanyBalance;

    @Builder.Default
    @OneToMany(mappedBy = "company")
    private List<Employee> employees = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Department> departments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyAdminAccess> adminAccessList = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void changeName(String newCompanyName) {
        this.companyName = newCompanyName;
    }

    public void changeStatus(String newStatus) {
        this.status = newStatus;
    }

    public void addBudget(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Budget amount cannot be negative.");
        }
        this.totalCompanyBalance += amount;
    }

    public void spendBudget(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to spend cannot be negative.");
        }
        if (this.totalCompanyBalance < amount) {
            throw new InsufficientPointsException("The company does not have enough budget to distribute the points.");
        }
        this.totalCompanyBalance -= amount;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
        if (this.totalCompanyBalance == null) {
            this.totalCompanyBalance = 0.0;
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}