package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Employee implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String employeeName;
    @Column(nullable = false, unique = true)
    private String email;
    private String position;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private UserRole role;

    // 사용자 셀프 서비스 필드
    @Column(length = 2048) // URL은 길 수 있으므로 길이 확장
    private String profileImageUrl;
    private String personalEmail;
    private String phoneNumber;
    private String shippingAddress;
    private Boolean emailNotificationEnabled;
    private Boolean appNotificationEnabled;
    private String language;
    private String timezone;

    private LocalDateTime lastLoginAt;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Wallet wallet;

    @Builder.Default
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RewardPointTransaction> sentTransactions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RewardPointTransaction> receivedTransactions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyAdminAccess> adminAccesses = new ArrayList<>();

    public void changePassword(String newEncodedPassword) {
        this.passwordHash = newEncodedPassword;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
    }

    public void assignToDepartment(Department newDepartment) {
        this.department = newDepartment;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.role == null) this.role = UserRole.EMPLOYEE;
        if (this.emailNotificationEnabled == null) this.emailNotificationEnabled = true;
        if (this.appNotificationEnabled == null) this.appNotificationEnabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // UserDetails 구현 메서드들...
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(this.status);
    }
}
