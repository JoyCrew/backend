package com.joycrew.backend.service;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.entity.Employee;
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

    // ... (getUserProfile 테스트는 DTO의 from 메서드를 사용하므로 큰 변경 없음)

    @Test
    @DisplayName("[Service] 비밀번호 변경 성공 - Employee의 changePassword 메서드 호출 검증")
    void forcePasswordChange_Success() {
        // Given
        String userEmail = "test@joycrew.com";
        // [수정] Record 타입 생성자 사용
        PasswordChangeRequest request = new PasswordChangeRequest("newPassword123!");

        // [수정] 실제 Employee 객체 대신 Mock 객체를 사용하여 상호작용(interaction)을 검증
        Employee mockEmployee = mock(Employee.class);
        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));

        // When
        employeeService.forcePasswordChange(userEmail, request);

        // Then
        // [수정] 서비스가 passwordEncoder를 직접 호출하는 대신,
        // Employee 객체의 changePassword 메서드에 올바른 인자를 넘겨 호출했는지 검증.
        // 이것이 바로 'Tell, Don't Ask' 원칙을 따르는 테스트.
        verify(mockEmployee, times(1)).changePassword(request.newPassword(), passwordEncoder);
        verify(employeeRepository, times(1)).save(mockEmployee); // 변경 후 저장 호출 확인
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
