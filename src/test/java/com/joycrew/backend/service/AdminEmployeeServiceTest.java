package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEmployeeServiceTest {

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
    private AdminEmployeeService adminEmployeeService;

    private EmployeeRegistrationRequest request;
    private Company mockCompany;

    @BeforeEach
    void setUp() {
        request = new EmployeeRegistrationRequest(
                "테스트유저",
                "test@joycrew.com",
                "password123!",
                "JoyCrew",
                "Engineering",
                "Developer",
                AdminLevel.EMPLOYEE
        );

        mockCompany = Company.builder().companyId(1L).companyName("JoyCrew").build();
    }

    @Test
    @DisplayName("[Service] 단일 직원 등록 성공")
    void registerEmployee_Success() {
        // Given
        Employee savedEmployee = Employee.builder().employeeId(1L).email(request.email()).build();

        when(employeeRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyName(request.companyName())).thenReturn(Optional.of(mockCompany));
        when(departmentRepository.findByCompanyAndName(any(), anyString())).thenReturn(Optional.of(mock(Department.class)));
        when(passwordEncoder.encode(request.initialPassword())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        // When
        Employee result = adminEmployeeService.registerEmployee(request);

        // Then
        assertThat(result).isEqualTo(savedEmployee);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("[Service] 단일 직원 등록 실패 - 이메일 중복")
    void registerEmployee_Failure_EmailAlreadyExists() {
        // Given
        when(employeeRepository.findByEmail(request.email())).thenReturn(Optional.of(mock(Employee.class)));

        // When & Then
        assertThatThrownBy(() -> adminEmployeeService.registerEmployee(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("[Service] 단일 직원 등록 실패 - 존재하지 않는 회사")
    void registerEmployee_Failure_CompanyNotFound() {
        // Given
        when(employeeRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyName(request.companyName())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminEmployeeService.registerEmployee(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회사명입니다.");
    }

    @Test
    @DisplayName("[Service] CSV 파일로 직원 대량 등록 성공")
    void registerEmployeesFromCsv_Success() throws IOException {
        // Given
        String csvContent = "name,email,initialPassword,companyName,departmentName,position,level\n" +
                "김조이,joy@joycrew.com,joy123,JoyCrew,Engineering,Developer,EMPLOYEE\n" +
                "박크루,crew@joycrew.com,crew123,JoyCrew,Product,PO,MANAGER";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyName(anyString())).thenReturn(Optional.of(mockCompany));
        when(departmentRepository.findByCompanyAndName(any(), anyString())).thenReturn(Optional.of(mock(Department.class)));

        // When
        adminEmployeeService.registerEmployeesFromCsv(file);

        // Then
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(employeeRepository, times(2)).save(any(Employee.class));
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("[Service] CSV 파일 대량 등록 시 일부 행 실패해도 계속 진행")
    void registerEmployeesFromCsv_PartialFailure() throws IOException {
        // Given
        String csvContent = "name,email,initialPassword,companyName,departmentName,position,level\n" +
                "김조이,joy@joycrew.com,joy123,JoyCrew,Engineering,Developer,EMPLOYEE\n" +
                "이실패,fail@joycrew.com,fail123,WrongCompany,None,Intern,EMPLOYEE\n" + // 실패할 행
                "박크루,crew@joycrew.com,crew123,JoyCrew,Product,PO,MANAGER";
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(employeeRepository.findByEmail("joy@joycrew.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("crew@joycrew.com")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyName("JoyCrew")).thenReturn(Optional.of(mockCompany));
        when(departmentRepository.findByCompanyAndName(any(), anyString())).thenReturn(Optional.of(mock(Department.class)));

        when(employeeRepository.findByEmail("fail@joycrew.com")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyName("WrongCompany")).thenReturn(Optional.empty());

        // When
        adminEmployeeService.registerEmployeesFromCsv(file);

        // Then
        verify(employeeRepository, times(3)).findByEmail(anyString());
        verify(employeeRepository, times(2)).save(any(Employee.class));
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }
}