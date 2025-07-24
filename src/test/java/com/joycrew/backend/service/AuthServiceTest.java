package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private LoginRequest testLoginRequest;
    private String encodedPassword;
    private String testToken = "mocked.jwt.token";

    @BeforeEach
    void setUp() {
        encodedPassword = new BCryptPasswordEncoder().encode("password123");

        testEmployee = Employee.builder()
                .employeeId(1L)
                .email("test@joycrew.com")
                .passwordHash(encodedPassword)
                .employeeName("테스트유저")
                .role(UserRole.EMPLOYEE)
                .status("ACTIVE")
                .build();

        testLoginRequest = new LoginRequest();
        testLoginRequest.setEmail("test@joycrew.com");
        testLoginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰과 사용자 정보 반환")
    void login_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(employeeRepository.findByEmail(testLoginRequest.getEmail())).thenReturn(Optional.of(testEmployee));
        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);

        // When
        LoginResponse response = authService.login(testLoginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(testToken);
        assertThat(response.getMessage()).isEqualTo("로그인 성공");
        assertThat(response.getUserId()).isEqualTo(testEmployee.getEmployeeId());
        assertThat(response.getEmail()).isEqualTo(testEmployee.getEmail());
        assertThat(response.getRole()).isEqualTo(testEmployee.getRole());

        // 메서드 호출 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, times(1)).findByEmail(testLoginRequest.getEmail());
        verify(jwtUtil, times(1)).generateToken(testEmployee.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 없음 (UsernameNotFoundException)")
    void login_Failure_EmailNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");

        // 메서드 호출 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 (BadCredentialsException)")
    void login_Failure_WrongPassword() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");

        // 메서드 호출 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }
}