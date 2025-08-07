package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.AdminPointDistributionRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminFeaturesIntegrationTest {

    @Autowired private AdminEmployeeService adminEmployeeService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private RewardPointTransactionRepository transactionRepository;

    private Employee admin, employee1, employee2;
    private Company company;

    @BeforeEach
    void setUp() {
        company = companyRepository.save(Company.builder().companyName("통합테스트회사").build());
        admin = createAndSaveEmployee("admin@test.com", "관리자", AdminLevel.SUPER_ADMIN, 0);
        employee1 = createAndSaveEmployee("emp1@test.com", "직원1", AdminLevel.EMPLOYEE, 100);
        employee2 = createAndSaveEmployee("emp2@test.com", "직원2", AdminLevel.EMPLOYEE, 200);
    }

    private Employee createAndSaveEmployee(String email, String name, AdminLevel level, int initialPoints) {
        Employee emp = Employee.builder().email(email).employeeName(name).role(level).company(company).passwordHash("...").build();
        employeeRepository.save(emp);
        Wallet wallet = new Wallet(emp);
        if (initialPoints > 0) {
            wallet.addPoints(initialPoints);
        }
        walletRepository.save(wallet);
        return emp;
    }

    @Test
    @DisplayName("[Integration] 관리자가 직원들에게 포인트를 성공적으로 분배")
    void distributePoints_Success() {
        // Given
        AdminPointDistributionRequest request = new AdminPointDistributionRequest(
                List.of(employee1.getEmployeeId(), employee2.getEmployeeId()),
                500,
                "보너스 지급",
                TransactionType.ADMIN_ADJUSTMENT
        );

        // When
        adminEmployeeService.distributePoints(request, admin);

        // Then
        Wallet wallet1 = walletRepository.findByEmployee_EmployeeId(employee1.getEmployeeId()).get();
        Wallet wallet2 = walletRepository.findByEmployee_EmployeeId(employee2.getEmployeeId()).get();
        assertThat(wallet1.getBalance()).isEqualTo(100 + 500);
        assertThat(wallet2.getBalance()).isEqualTo(200 + 500);
        assertThat(transactionRepository.findAll()).anyMatch(tx ->
                tx.getReceiver().equals(employee1) && tx.getPointAmount() == 500
        );
    }

    @Test
    @DisplayName("[Integration] 관리자가 직원의 상태를 성공적으로 DELETED로 변경 (소프트 삭제)")
    void deleteEmployee_Success() {
        // When
        adminEmployeeService.disableEmployee(employee1.getEmployeeId());

        // Then
        Employee deletedEmployee = employeeRepository.findById(employee1.getEmployeeId()).get();
        assertThat(deletedEmployee.getStatus()).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("[Integration] 관리자가 직원의 직책을 성공적으로 업데이트")
    void updateEmployee_Success() {
        // Given
        AdminEmployeeUpdateRequest request = new AdminEmployeeUpdateRequest(null, null, "선임 연구원", null, null);

        // When
        adminEmployeeService.updateEmployee(employee1.getEmployeeId(), request);

        // Then
        Employee updatedEmployee = employeeRepository.findById(employee1.getEmployeeId()).get();
        assertThat(updatedEmployee.getPosition()).isEqualTo("선임 연구원");
    }
}