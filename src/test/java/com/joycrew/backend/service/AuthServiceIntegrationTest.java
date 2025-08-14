package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class AuthServiceIntegrationTest {

  @Autowired private AuthService authService;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private EmployeeRegistrationService registrationService;

  private String testEmail = "integration@joycrew.com";
  private String testPassword = "integrationPass123!";
  private String testName = "IntegrationTestUser";
  private Company defaultCompany;

  @BeforeEach
  void setUp() {
    defaultCompany = companyRepository.save(Company.builder().companyName("Test Company").build());
    employeeRepository.findByEmail(testEmail).ifPresent(employeeRepository::delete);

    EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
        testName, testEmail, testPassword,
        defaultCompany.getCompanyName(), null, "Staff",
        AdminLevel.EMPLOYEE, null, null, null
    );
    registrationService.registerEmployee(request);
  }

  @Test
  @DisplayName("[Integration] Login success returns JWT and user info")
  void login_Integration_Success() {
    // Given
    LoginRequest request = new LoginRequest(testEmail, testPassword);

    // When
    LoginResponse response = authService.login(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.message()).isEqualTo("Login successful");
    assertThat(response.email()).isEqualTo(testEmail);
  }

  @Test
  @DisplayName("[Integration] Login failure - Non-existent email")
  void login_Integration_Failure_EmailNotFound() {
    // Given
    LoginRequest request = new LoginRequest("nonexistent@joycrew.com", "anypassword");

    // When & Then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  @DisplayName("[Integration] Login failure - Wrong password")
  void login_Integration_Failure_WrongPassword() {
    // Given
    LoginRequest request = new LoginRequest(testEmail, "wrongpassword");

    // When & Then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }
}