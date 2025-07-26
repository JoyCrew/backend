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
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private WalletRepository walletRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Company testCompany;
    private Department testDepartment;
    private Employee testEmployeeWithWallet;
    private Employee testEmployeeWithoutWallet;
    private Wallet testWallet;

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

        testEmployeeWithWallet = Employee.builder()
                .company(testCompany)
                .department(testDepartment)
                .email("walletuser@joycrew.com")
                .passwordHash(passwordEncoder.encode("pass123"))
                .employeeName("지갑유저")
                .position("선임")
                .status("ACTIVE")
                .role(UserRole.EMPLOYEE)
                .build();
        entityManager.persist(testEmployeeWithWallet);

        testEmployeeWithoutWallet = Employee.builder()
                .company(testCompany)
                .department(testDepartment)
                .email("nowallet@joycrew.com")
                .passwordHash(passwordEncoder.encode("pass123"))
                .employeeName("지갑없는유저")
                .position("주니어")
                .status("ACTIVE")
                .role(UserRole.EMPLOYEE)
                .build();
        entityManager.persist(testEmployeeWithoutWallet);


        testWallet = Wallet.builder()
                .employee(testEmployeeWithWallet)
                .balance(5000)
                .giftablePoint(500)
                .build();
        entityManager.persist(testWallet);

        testEmployeeWithWallet.setWallet(testWallet);
        entityManager.merge(testEmployeeWithWallet);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Employee ID로 Wallet 조회 성공")
    void findByEmployee_EmployeeId_Success() {
        // When
        Optional<Wallet> foundWallet = walletRepository.findByEmployee_EmployeeId(testEmployeeWithWallet.getEmployeeId());

        // Then
        assertThat(foundWallet).isPresent();
        assertThat(foundWallet.get().getEmployee().getEmployeeId()).isEqualTo(testEmployeeWithWallet.getEmployeeId());
        assertThat(foundWallet.get().getBalance()).isEqualTo(5000);
        assertThat(foundWallet.get().getGiftablePoint()).isEqualTo(500);
    }

    @Test
    @DisplayName("Employee ID로 Wallet 조회 실패 - Wallet 없음")
    void findByEmployee_EmployeeId_NotFound() {
        // When
        Optional<Wallet> foundWallet = walletRepository.findByEmployee_EmployeeId(testEmployeeWithoutWallet.getEmployeeId());

        // Then
        assertThat(foundWallet).isEmpty();
    }

    @Test
    @DisplayName("Wallet 저장 및 조회 성공")
    void saveAndFindWallet() {
        // Given
        Employee anotherEmployee = Employee.builder()
                .company(testCompany)
                .department(testDepartment)
                .email("another@joycrew.com")
                .passwordHash(passwordEncoder.encode("pass456"))
                .employeeName("다른직원")
                .position("팀장")
                .status("ACTIVE")
                .role(UserRole.MANAGER)
                .build();
        entityManager.persist(anotherEmployee);

        Wallet newWallet = Wallet.builder()
                .employee(anotherEmployee)
                .balance(2000)
                .giftablePoint(200)
                .build();

        // When
        Wallet savedWallet = walletRepository.save(newWallet);
        Optional<Wallet> found = walletRepository.findById(savedWallet.getWalletId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualTo(2000);
        assertThat(found.get().getEmployee().getEmployeeId()).isEqualTo(anotherEmployee.getEmployeeId());

        anotherEmployee.setWallet(savedWallet);
        entityManager.merge(anotherEmployee);
        entityManager.flush();
        entityManager.clear();

        Optional<Employee> foundEmployee = employeeRepository.findById(anotherEmployee.getEmployeeId());
        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getWallet()).isNotNull();
        assertThat(foundEmployee.get().getWallet().getWalletId()).isEqualTo(savedWallet.getWalletId());
    }
}