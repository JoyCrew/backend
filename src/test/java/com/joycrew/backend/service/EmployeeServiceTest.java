package com.joycrew.backend.service;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("[Unit] Get profile success - Wallet exists")
    void getUserProfile_Success_WalletExists() {
        // Given
        String userEmail = "test@joycrew.com";
        Employee mockEmployee = Employee.builder().employeeId(1L).email(userEmail).employeeName("Test User").build();
        Wallet mockWallet = new Wallet(mockEmployee);
        mockWallet.addPoints(200);

        UserProfileResponse mockDto = new UserProfileResponse(1L, "Test User", userEmail, null, 200, 200, AdminLevel.EMPLOYEE, null, null, null, null, null);

        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(mockWallet));
        when(employeeMapper.toUserProfileResponse(any(Employee.class), any(Wallet.class))).thenReturn(mockDto);

        // When
        UserProfileResponse response = employeeService.getUserProfile(userEmail);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.totalBalance()).isEqualTo(200);
    }

    @Test
    @DisplayName("[Unit] Get profile success - Wallet does not exist (defaults to 0)")
    void getUserProfile_Success_WalletDoesNotExist() {
        // Given
        String userEmail = "test@joycrew.com";
        Employee mockEmployee = Employee.builder().employeeId(1L).email(userEmail).employeeName("Test User").build();
        UserProfileResponse mockDto = new UserProfileResponse(1L, "Test User", userEmail, null, 0, 0, AdminLevel.EMPLOYEE, null, null, null, null, null);

        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.empty());
        when(employeeMapper.toUserProfileResponse(any(Employee.class), any(Wallet.class))).thenReturn(mockDto);


        // When
        UserProfileResponse response = employeeService.getUserProfile(userEmail);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.totalBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("[Unit] Change password success - Verifies changePassword call")
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
    @DisplayName("[Unit] Change password failure - User not found")
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