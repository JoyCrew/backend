package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EmployeeRepository employeeRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Company testCompany;
    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .companyName("테스트회사")
                .status("ACTIVE")
                .startAt(LocalDateTime.now())
                .totalCompanyBalance(0.0)
                .build();
        entityManager.persist(testCompany);

        testDepartment = Department.builder()
                .name("테스트부서")
                .company(testCompany)
                .build();
        entityManager.persist(testDepartment);

        testEmployee = Employee.builder()
                .company(testCompany)
                .department(testDepartment)
                .email("test@joycrew.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .employeeName("김테스트")
                .position("사원")
                .status("ACTIVE")
                .role(UserRole.EMPLOYEE)
                .lastLoginAt(null)
                .build();
        entityManager.persist(testEmployee);

        Wallet testWallet = Wallet.builder()
                .employee(testEmployee)
                .balance(1000)
                .giftablePoint(100)
                .build();
        entityManager.persist(testWallet);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("이메일로 직원 조회 성공")
    void findByEmail_Success() {
        // When
        Optional<Employee> foundEmployee = employeeRepository.findByEmail("test@joycrew.com");

        // Then
        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getEmail()).isEqualTo("test@joycrew.com");
        assertThat(foundEmployee.get().getEmployeeName()).isEqualTo("김테스트");
        assertThat(foundEmployee.get().getCompany().getCompanyName()).isEqualTo("테스트회사");
        assertThat(foundEmployee.get().getDepartment().getName()).isEqualTo("테스트부서");
    }

    @Test
    @DisplayName("이메일로 직원 조회 실패 - 존재하지 않는 이메일")
    void findByEmail_NotFound() {
        // When
        Optional<Employee> foundEmployee = employeeRepository.findByEmail("nonexistent@joycrew.com");

        // Then
        assertThat(foundEmployee).isEmpty();
    }

    @Test
    @DisplayName("Employee 저장 및 조회 성공")
    void saveAndFindEmployee() {
        // Given
        Employee newEmployee = Employee.builder()
                .company(testCompany)
                .department(testDepartment)
                .email("new@joycrew.com")
                .passwordHash(passwordEncoder.encode("newpass"))
                .employeeName("새로운직원")
                .position("대리")
                .status("ACTIVE")
                .role(UserRole.EMPLOYEE)
                .build();

        // When
        Employee savedEmployee = employeeRepository.save(newEmployee);
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getEmployeeId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("new@joycrew.com");
        assertThat(found.get().getEmployeeName()).isEqualTo("새로운직원");
    }
}