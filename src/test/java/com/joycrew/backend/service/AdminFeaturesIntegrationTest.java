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

    @Autowired private EmployeeManagementService managementService;
    @Autowired private AdminPointService pointService;

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private RewardPointTransactionRepository transactionRepository;

    private Employee admin, employee1, employee2;
    private Company company;

    @BeforeEach
    void setUp() {
        company = companyRepository.save(Company.builder().companyName("Integration Test Company").build());
        admin = createAndSaveEmployee("admin@test.com", "Admin", AdminLevel.SUPER_ADMIN, 0);
        employee1 = createAndSaveEmployee("emp1@test.com", "Employee1", AdminLevel.EMPLOYEE, 100);
        employee2 = createAndSaveEmployee("emp2@test.com", "Employee2", AdminLevel.EMPLOYEE, 200);
    }

    private Employee createAndSaveEmployee(String email, String name, AdminLevel level, int initialPoints) {
        Employee emp = Employee.builder().email(email).employeeName(name).role(level).company(company).passwordHash("...").status("ACTIVE").build();
        employeeRepository.save(emp);
        Wallet wallet = new Wallet(emp);
        if (initialPoints > 0) {
            wallet.addPoints(initialPoints);
        }
        walletRepository.save(wallet);
        return emp;
    }

    @Test
    @DisplayName("[Integration] Admin successfully distributes points to employees")
    void distributePoints_Success() {
        // Given
        AdminPointDistributionRequest request = new AdminPointDistributionRequest(
                List.of(employee1.getEmployeeId(), employee2.getEmployeeId()),
                500,
                "Bonus Payout",
                TransactionType.ADMIN_ADJUSTMENT
        );

        // When
        pointService.distributePoints(request, admin);

        // Then
        Wallet wallet1 = walletRepository.findByEmployee_EmployeeId(employee1.getEmployeeId()).get();
        Wallet wallet2 = walletRepository.findByEmployee_EmployeeId(employee2.getEmployeeId()).get();
        assertThat(wallet1.getBalance()).isEqualTo(100 + 500);
        assertThat(wallet2.getBalance()).isEqualTo(200 + 500);
    }

    @Test
    @DisplayName("[Integration] Admin successfully deactivates an employee (soft delete)")
    void deactivateEmployee_Success() {
        // When
        managementService.deactivateEmployee(employee1.getEmployeeId());

        // Then
        Employee deletedEmployee = employeeRepository.findById(employee1.getEmployeeId()).get();
        assertThat(deletedEmployee.getStatus()).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("[Integration] Admin successfully updates an employee's position")
    void updateEmployee_Success() {
        // Given
        AdminEmployeeUpdateRequest request = new AdminEmployeeUpdateRequest(null, null, "Senior Researcher", null, null);

        // When
        managementService.updateEmployee(employee1.getEmployeeId(), request);

        // Then
        Employee updatedEmployee = employeeRepository.findById(employee1.getEmployeeId()).get();
        assertThat(updatedEmployee.getPosition()).isEqualTo("Senior Researcher");
    }
}