package com.joycrew.backend.service;

import com.joycrew.backend.JoyCrewBackendApplication;
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

@SpringBootTest(classes = JoyCrewBackendApplication.class)
@ActiveProfiles("dev")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CompanyRepository companyRepository;

    private String testEmail = "integration@joycrew.com";
    private String testPassword = "integrationPass123!";
    private String testName = "통합테스트유저";
    private Company defaultCompany;
    @Autowired
    private AdminEmployeeService adminEmployeeService;

    @BeforeEach
    void setUp() {
        defaultCompany = companyRepository.save(Company.builder().companyName("테스트컴퍼니").build());
        employeeRepository.findByEmail(testEmail).ifPresent(employeeRepository::delete);

        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                testName,
                testEmail,
                testPassword,
                defaultCompany.getCompanyName(),
                null,
                "사원",
                AdminLevel.EMPLOYEE,
                null, null, null // birthday, address, hireDate
        );
        adminEmployeeService.registerEmployee(request);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 성공 시 JWT 토큰과 사용자 정보 반환")
    void login_Integration_Success() {
        // Given
        LoginRequest request = new LoginRequest(testEmail, testPassword);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.email()).isEqualTo(testEmail);
        assertThat(response.userId()).isEqualTo(employeeRepository.findByEmail(testEmail).get().getEmployeeId());
        assertThat(response.name()).isEqualTo(testName);
        assertThat(response.role()).isEqualTo(AdminLevel.EMPLOYEE);

        String extractedEmail = jwtUtil.getEmailFromToken(response.accessToken());
        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 실패 - 존재하지 않는 이메일")
    void login_Integration_Failure_EmailNotFound() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@joycrew.com", "anypassword");

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 실패 - 비밀번호 불일치")
    void login_Integration_Failure_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest(testEmail, "wrongpassword");

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
