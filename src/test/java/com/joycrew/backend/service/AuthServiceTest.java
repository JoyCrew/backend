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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private WalletRepository walletRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private LoginRequest testLoginRequest;
    private final String testToken = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMs", 900000L);

        testEmployee = Employee.builder()
                .employeeId(1L)
                .email("test@joycrew.com")
                .passwordHash("encodedPassword")
                .employeeName("Test User")
                .role(AdminLevel.EMPLOYEE)
                .status("ACTIVE")
                .profileImageUrl("http://example.com/profile.jpg")
                .build();

        testLoginRequest = new LoginRequest("test@joycrew.com", "password123");
    }

    @Test
    @DisplayName("[Unit] Login success should return JWT and user info")
    void login_Success() {
        // Given
        UserPrincipal principal = new UserPrincipal(testEmployee);
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);

        Wallet mockWallet = mock(Wallet.class);
        when(mockWallet.getBalance()).thenReturn(1000);
        when(walletRepository.findByEmployee_EmployeeId(anyLong())).thenReturn(Optional.of(mockWallet));

        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);

        // When
        LoginResponse response = authService.login(testLoginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(testToken);
        assertThat(response.message()).isEqualTo("Login successful"); // 수정된 부분
        assertThat(response.userId()).isEqualTo(testEmployee.getEmployeeId());
        assertThat(response.email()).isEqualTo(testEmployee.getEmail());
        assertThat(response.totalPoint()).isEqualTo(1000);
        assertThat(response.profileImageUrl()).isEqualTo(testEmployee.getProfileImageUrl());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(testEmployee.getEmail());
    }

    @Test
    @DisplayName("[Unit] Login failure should throw BadCredentialsException")
    void login_Failure_WrongPassword() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("[Unit] Login failure should throw UsernameNotFoundException")
    void login_Failure_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}