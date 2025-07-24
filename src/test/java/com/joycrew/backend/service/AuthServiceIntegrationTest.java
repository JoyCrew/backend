package com.joycrew.backend.service;

import com.joycrew.backend.JoyCrewBackendApplication;
import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.enums.UserRole;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = JoyCrewBackendApplication.class)
@ActiveProfiles("dev")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private EmployeeService employeeService;
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

    @BeforeEach
    void setUp() {
        defaultCompany = Company.builder()
                .companyName("테스트컴퍼니")
                .status("ACTIVE")
                .startAt(LocalDateTime.now())
                .totalCompanyBalance(0.0)
                .build();
        defaultCompany = companyRepository.save(defaultCompany);

        employeeRepository.findByEmail(testEmail).ifPresent(employeeRepository::delete);

        employeeService.registerEmployee(testEmail, testPassword, testName, defaultCompany);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 성공 시 JWT 토큰과 사용자 정보 반환")
    void login_Integration_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getMessage()).isEqualTo("로그인 성공");
        assertThat(response.getEmail()).isEqualTo(testEmail);
        assertThat(response.getUserId()).isEqualTo(employeeRepository.findByEmail(testEmail).get().getEmployeeId());
        assertThat(response.getName()).isEqualTo(testName);
        assertThat(response.getRole()).isEqualTo(UserRole.EMPLOYEE);

        String extractedEmail = jwtUtil.getEmailFromToken(response.getAccessToken());
        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 실패 - 존재하지 않는 이메일")
    void login_Integration_Failure_EmailNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@joycrew.com");
        request.setPassword("anypassword");

        // When & Then
        // 발생하는 예외의 종류만 확인하도록 수정
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("통합 테스트: 로그인 실패 - 비밀번호 불일치")
    void login_Integration_Failure_WrongPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("wrongpassword");

        // When & Then
        // 발생하는 예외의 종류만 확인하도록 수정
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
