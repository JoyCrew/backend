package com.joycrew.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_verification",
        indexes = {
                @Index(name = "idx_pv_phone_created", columnList = "phone, createdAt")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PhoneVerification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String codeHash; // BCrypt hash

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private int maxAttempts;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastSentAt;

    @Column(nullable = false, unique = true, length = 36)
    private String requestId; // UUID 문자열

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    public enum Status { PENDING, VERIFIED, EXPIRED, BLOCKED }
}