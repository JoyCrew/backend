// src/main/java/com/joycrew/backend/entity/CompanyDomain.java
package com.joycrew.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_domain")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyDomain {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, unique = true, length = 255)
    private String domain;

    @Column(name = "primary_domain", nullable = false)
    private Boolean primaryDomain;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void pre(){ createdAt = updatedAt = LocalDateTime.now(); if (primaryDomain==null) primaryDomain=true; }
    @PreUpdate  void upd(){ updatedAt = LocalDateTime.now(); }
}
