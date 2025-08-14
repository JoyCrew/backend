package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EmployeeServiceIntegrationTest {

  @Autowired private EmployeeService employeeService;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private WalletRepository walletRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private DepartmentRepository departmentRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private EmployeeRegistrationService registrationService;

  private Company testCompany;
  private Department testDepartment;

  @BeforeEach
  void setUp() {
    testCompany = companyRepository.save(Company.builder().companyName("Test Company").build());
    testDepartment = departmentRepository.save(Department.builder().name("Test Department").company(testCompany).build());
  }

  @Test
  @DisplayName("[Integration] Register new employee successfully")
  void registerEmployee_Success() {
    // Given
    EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
        "New Employee", "new.employee@joycrew.com", "password123!",
        testCompany.getCompanyName(), testDepartment.getName(), "Staff", AdminLevel.EMPLOYEE,
        LocalDate.of(1998, 1, 1), "Seoul", LocalDate.now()
    );

    // When
    Employee savedEmployee = registrationService.registerEmployee(request);

    // Then
    assertThat(savedEmployee.getEmployeeId()).isNotNull();
    assertThat(savedEmployee.getEmail()).isEqualTo("new.employee@joycrew.com");
    assertThat(walletRepository.findByEmployee_EmployeeId(savedEmployee.getEmployeeId())).isPresent();
  }

  @Test
  @DisplayName("[Integration] Change employee password successfully")
  void forcePasswordChange_Success() {
    // Given
    Employee employee = employeeRepository.save(Employee.builder()
        .email("pw.change@joycrew.com")
        .employeeName("Password Changer")
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