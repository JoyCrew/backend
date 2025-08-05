package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private LoginRequest testLoginRequest;
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
                .build();

        testLoginRequest = new LoginRequest("test@joycrew.com", "password123");
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰과 사용자 정보 반환")
    void login_Success() {
        // Given
        UserPrincipal principal = new UserPrincipal(testEmployee);
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);

        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);

        // When
        LoginResponse response = authService.login(testLoginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(testToken);
        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.userId()).isEqualTo(testEmployee.getEmployeeId());
        assertThat(response.email()).isEqualTo(testEmployee.getEmail());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(testEmployee.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 자격 증명 오류 (BadCredentialsException)")
    void login_Failure_WrongPassword() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}