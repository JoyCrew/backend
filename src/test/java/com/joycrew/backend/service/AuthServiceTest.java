package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
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
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private WalletRepository walletRepository;

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

        Wallet mockWallet = mock(Wallet.class);
        when(mockWallet.getBalance()).thenReturn(1000);
        when(mockWallet.getGiftablePoint()).thenReturn(500);
        when(walletRepository.findByEmployee_EmployeeId(anyLong())).thenReturn(Optional.of(mockWallet));

        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);

        // When
        LoginResponse response = authService.login(testLoginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(testToken);
        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.userId()).isEqualTo(testEmployee.getEmployeeId());
        assertThat(response.email()).isEqualTo(testEmployee.getEmail());
        assertThat(response.role()).isEqualTo(testEmployee.getRole());
        // assertThat(response.totalPoint()).isEqualTo(1000); // DTO에 totalPoint 필드가 있다면 추가
        // assertThat(response.profileImageUrl()).isEqualTo(testEmployee.getProfileImageUrl()); // DTO에 profileImageUrl 필드가 있다면 추가

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(testEmployee.getEmail());
        verify(walletRepository, times(1)).findByEmployee_EmployeeId(testEmployee.getEmployeeId());
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

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        // WalletRepository는 호출되지 않음
        verify(walletRepository, never()).findByEmployee_EmployeeId(anyLong());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음 (UsernameNotFoundException)")
    void login_Failure_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When & Then
        assertThatThrownBy(() -> authService.login(testLoginRequest))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(walletRepository, never()).findByEmployee_EmployeeId(anyLong());
    }
}