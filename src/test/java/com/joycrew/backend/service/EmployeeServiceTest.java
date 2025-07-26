package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("[Service] 신규 직원 등록 성공")
    void registerEmployee_Success() {
        // Given
        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest();
        request.setEmail("new@joycrew.com");
        request.setName("신규직원");
        request.setInitialPassword("password123!");
        request.setCompanyId(1L);
        request.setDepartmentId(10L);
        request.setPosition("사원");
        request.setRole(UserRole.EMPLOYEE);

        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(companyRepository.findById(1L)).thenReturn(Optional.of(new Company()));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(new Department()));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Employee result = employeeService.registerEmployee(request);

        // Then
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        assertThat(result.getPasswordHash()).isEqualTo("encodedPassword");
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("[Service] 직원 정보 수정 (관리자) 성공")
    void updateEmployeeDetailsByAdmin_Success() {
        // Given
        Long employeeId = 1L;
        AdminEmployeeUpdateRequest request = new AdminEmployeeUpdateRequest();
        request.setName("이름변경");
        request.setPosition("대리");

        Employee existingEmployee = Employee.builder().employeeId(employeeId).employeeName("기존이름").position("사원").build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Employee result = employeeService.updateEmployeeDetailsByAdmin(employeeId, request);

        // Then
        assertThat(result.getEmployeeName()).isEqualTo("이름변경");
        assertThat(result.getPosition()).isEqualTo("대리");
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    @DisplayName("[Service] 비밀번호 변경 (첫 로그인) 성공")
    void forcePasswordChange_Success() {
        // Given
        String userEmail = "test@joycrew.com";
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setNewPassword("newPassword123!");

        Employee existingEmployee = Employee.builder().email(userEmail).passwordHash("oldPassword").build();
        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(existingEmployee));
        when(passwordEncoder.encode("newPassword123!")).thenReturn("newEncodedPassword");

        // When
        employeeService.forcePasswordChange(userEmail, request);

        // Then
        verify(employeeRepository, times(1)).save(existingEmployee);
        assertThat(existingEmployee.getPasswordHash()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("[Service] 직원 등록 실패 - 이메일 중복")
    void registerEmployee_Failure_EmailDuplicate() {
        // Given
        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest();
        request.setEmail("duplicate@joycrew.com");

        when(employeeRepository.findByEmail("duplicate@joycrew.com")).thenReturn(Optional.of(new Employee()));

        // When & Then
        assertThatThrownBy(() -> employeeService.registerEmployee(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }
}
