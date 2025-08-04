package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private String testToken = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .employeeId(1L)
                .email("test@joycrew.com")
                .passwordHash("encodedPassword")
                .employeeName("테스트유저")
                .role(AdminLevel.EMPLOYEE)
                .status("ACTIVE")
                .profileImageUrl("http://example.com/profile.jpg")
                .build();
    }

    @Test
    @DisplayName("[Service] 로그인 성공")
    void login_Success() {
        // Given
        LoginRequest testLoginRequest = new LoginRequest("test@joycrew.com", "password123");
        UserPrincipal principal = new UserPrincipal(testEmployee);
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        Wallet mockWallet = new Wallet(testEmployee);
        mockWallet.addPoints(100);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(mockWallet));
        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);

        // When
        LoginResponse response = authService.login(testLoginRequest);

        // Then
        assertThat(response.accessToken()).isEqualTo(testToken);
        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.totalPoint()).isEqualTo(100);
        assertThat(response.profileImageUrl()).isEqualTo(testEmployee.getProfileImageUrl());
    }

    @Test
    @DisplayName("[Service] 로그인 실패 - 자격 증명 오류")
    void login_Failure_WrongPassword() {
        // Given
        LoginRequest testLoginRequest = new LoginRequest("test@joycrew.com", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("[Service] 비밀번호 재설정 요청 성공 - 이메일 존재")
    void requestPasswordReset_Success_EmailExists() {
        // Given
        String email = "test@joycrew.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
        when(jwtUtil.generateToken(eq(email), anyLong())).thenReturn(testToken);

        // When
        authService.requestPasswordReset(email);

        // Then
        verify(emailService, times(1)).sendPasswordResetEmail(email, testToken);
    }

    @Test
    @DisplayName("[Service] 비밀번호 재설정 요청 성공 - 이메일 존재하지 않음 (공격 방지)")
    void requestPasswordReset_Success_EmailDoesNotExist() {
        // Given
        String email = "notfound@joycrew.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        authService.requestPasswordReset(email);

        // Then
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("[Service] 비밀번호 재설정 확인 성공")
    void confirmPasswordReset_Success() {
        // Given
        String newPassword = "newPassword123!";
        when(jwtUtil.getEmailFromToken(testToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail())).thenReturn(Optional.of(testEmployee));

        // When
        authService.confirmPasswordReset(testToken, newPassword);

        // Then
        verify(testEmployee, times(1)).changePassword(newPassword, passwordEncoder);
    }

    @Test
    @DisplayName("[Service] 비밀번호 재설정 확인 실패 - 유효하지 않은 토큰")
    void confirmPasswordReset_Failure_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtil.getEmailFromToken(invalidToken)).thenThrow(new JwtException("Invalid Token"));

        // When & Then
        assertThatThrownBy(() -> authService.confirmPasswordReset(invalidToken, "newPassword"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("유효하지 않거나 만료된 토큰입니다.");
    }
}