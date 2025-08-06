package com.joycrew.backend.service;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("[Service] 프로필 조회 성공 - 지갑 존재")
    void getUserProfile_Success_WalletExists() {
        // Given
        String userEmail = "test@joycrew.com";
        Employee mockEmployee = Employee.builder().employeeId(1L).email(userEmail).employeeName("테스트유저").build();
        Wallet mockWallet = new Wallet(mockEmployee);
        mockWallet.addPoints(200);

        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(mockWallet));

        // When
        UserProfileResponse response = employeeService.getUserProfile(userEmail);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트유저");         // getName() -> name()으로 수정
        assertThat(response.totalBalance()).isEqualTo(200);   // getPointBalance() -> totalBalance()로 수정
    }

    @Test
    @DisplayName("[Service] 프로필 조회 성공 - 지갑 없음 (기본값 0으로 생성)")
    void getUserProfile_Success_WalletDoesNotExist() {
        // Given
        String userEmail = "test@joycrew.com";
        Employee mockEmployee = Employee.builder().employeeId(1L).email(userEmail).employeeName("테스트유저").build();

        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.empty());

        // When
        UserProfileResponse response = employeeService.getUserProfile(userEmail);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트유저");
        assertThat(response.totalBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("[Service] 비밀번호 변경 성공 - Employee의 changePassword 메서드 호출 검증")
    void forcePasswordChange_Success() {
        // Given
        String userEmail = "test@joycrew.com";
        PasswordChangeRequest request = new PasswordChangeRequest("newPassword123!");
        Employee mockEmployee = mock(Employee.class);
        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));

        // When
        employeeService.forcePasswordChange(userEmail, request);

        // Then
        verify(mockEmployee, times(1)).changePassword(request.newPassword(), passwordEncoder);
    }

    @Test
    @DisplayName("[Service] 비밀번호 변경 실패 - 사용자를 찾을 수 없음")
    void forcePasswordChange_Failure_UserNotFound() {
        // Given
        String userEmail = "notfound@joycrew.com";
        PasswordChangeRequest request = new PasswordChangeRequest("newPassword123!");

        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> employeeService.forcePasswordChange(userEmail, request))
                .isInstanceOf(UserNotFoundException.class);
    }
}