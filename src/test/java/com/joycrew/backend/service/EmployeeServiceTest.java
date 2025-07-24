package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Company mockCompany;
    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, passwordEncoder, walletRepository);

        when(mockCompany.getCompanyId()).thenReturn(1L);
    }

    @Test
    @DisplayName("직원 등록 성공")
    void registerEmployee_Success() {
        // Given
        String email = "newuser@joycrew.com";
        String rawPassword = "newpassword123";
        String name = "새로운직원";
        String encodedPassword = "encodedPasswordHash";

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee savedEmployee = invocation.getArgument(0);
            if (savedEmployee.getEmployeeId() == null) {
                savedEmployee.setEmployeeId(2L);
            }
            return savedEmployee;
        });

        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        employeeService.registerEmployee(email, rawPassword, name, mockCompany);

        // Then
        verify(employeeRepository, times(2)).save(any(Employee.class));
        verify(employeeRepository, times(2)).save(argThat(employee ->
                employee.getEmail().equals(email) &&
                        employee.getPasswordHash().equals(encodedPassword) &&
                        employee.getEmployeeName().equals(name) &&
                        employee.getStatus().equals("ACTIVE") &&
                        employee.getRole().equals(UserRole.EMPLOYEE) &&
                        employee.getCompany().getCompanyId().equals(mockCompany.getCompanyId())
        ));
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("직원 등록 실패 - 이메일 중복")
    void registerEmployee_Failure_EmailDuplicate() {
        // Given
        String email = "existing@joycrew.com";
        String rawPassword = "password123";
        String name = "기존직원";

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(Employee.builder().email(email).build()));

        // When & Then
        assertThatThrownBy(() -> employeeService.registerEmployee(email, rawPassword, name, mockCompany))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다.");

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}