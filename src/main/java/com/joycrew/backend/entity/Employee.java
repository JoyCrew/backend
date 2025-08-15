package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.AdminLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Where(clause = "status = 'ACTIVE'")
@Builder
public class Employee {

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
  @Column(nullable = false, columnDefinition = "VARCHAR(255)")
  @Enumerated(EnumType.STRING)
  private AdminLevel role;

  @Column(length = 2048)
  private String profileImageUrl;
  private String personalEmail;
  private String phoneNumber;
  private String shippingAddress;
  private LocalDate birthday;
  private String address;
  private LocalDate hireDate;
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

  public boolean isActive() {
    return "ACTIVE".equals(this.status);
  }

  public void updateLastLogin() {
    this.lastLoginAt = LocalDateTime.now();
  }

  public void assignToDepartment(Department newDepartment) {
    this.department = newDepartment;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = this.updatedAt = LocalDateTime.now();
    if (this.status == null) this.status = "ACTIVE";
    if (this.role == null) this.role = AdminLevel.EMPLOYEE;
    if (this.emailNotificationEnabled == null) this.emailNotificationEnabled = true;
    if (this.appNotificationEnabled == null) this.appNotificationEnabled = true;
  }

  @PreUpdate
  protected void onUpdate() {
    if (this.status == null || !Arrays.asList("ACTIVE", "INACTIVE", "PENDING").contains(this.status)) {
      this.status = "ACTIVE";
    }
    this.updatedAt = LocalDateTime.now();
  }
  public void changePassword(String rawPassword, PasswordEncoder encoder) {
    this.passwordHash = encoder.encode(rawPassword);
  }

  public void updateName(String newName) {
    this.employeeName = newName;
  }

  public void updatePosition(String newPosition) {
    this.position = newPosition;
  }

  public void updateRole(AdminLevel newRole) {
    this.role = newRole;
  }

  public void updateStatus(String newStatus) {
    if (newStatus != null && Arrays.asList("ACTIVE", "INACTIVE", "PENDING").contains(newStatus)) {
      this.status = newStatus;
    } else {
      this.status = "ACTIVE";
    }
  }

  public void updateProfileImageUrl(String newUrl) {
    this.profileImageUrl = newUrl;
  }

  public void updatePersonalEmail(String newEmail) {
    this.personalEmail = newEmail;
  }

  public void updatePhoneNumber(String newNumber) {
    this.phoneNumber = newNumber;
  }

  public void updateBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public void updateAddress(String address) {
    this.address = address;
  }
}
