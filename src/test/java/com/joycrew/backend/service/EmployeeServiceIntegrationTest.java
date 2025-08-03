package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company testCompany;
    private Department testDepartment;
    @Autowired
    private AdminEmployeeService adminEmployeeService;

    @BeforeEach
    void setUp() {
        testCompany = companyRepository.save(Company.builder().companyName("테스트 회사").build());
        testDepartment = departmentRepository.save(Department.builder().name("테스트 부서").company(testCompany).build());
    }

    @Test
    @DisplayName("[Integration] 신규 직원 등록 성공")
    void registerEmployee_Success() {
        // Given
        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                "신규직원", "new.employee@joycrew.com", "password123!",
                testCompany.getCompanyName(), testDepartment.getName(), "사원", UserRole.EMPLOYEE
        );

        // When
        Employee savedEmployee = adminEmployeeService.registerEmployee(request);

        // Then
        assertThat(savedEmployee.getEmployeeId()).isNotNull();
        assertThat(savedEmployee.getEmail()).isEqualTo("new.employee@joycrew.com");
        assertThat(walletRepository.findByEmployee_EmployeeId(savedEmployee.getEmployeeId())).isPresent();
    }

    @Test
    @DisplayName("[Integration] 직원 비밀번호 변경 성공")
    void forcePasswordChange_Success() {
        // Given
        Employee employee = employeeRepository.save(Employee.builder()
                .email("pw.change@joycrew.com")
                .employeeName("패스워드변경")
                .passwordHash(passwordEncoder.encode("oldPassword"))
                .company(testCompany)
                .build());

        PasswordChangeRequest request = new PasswordChangeRequest("newPassword123!");

        // When
        employeeService.forcePasswordChange(employee.getEmail(), request);

        // Then
        Employee updatedEmployee = employeeRepository.findByEmail(employee.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword123!", updatedEmployee.getPasswordHash())).isTrue();
    }
}
