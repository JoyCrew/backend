package com.joycrew.backend.service;

import com.joycrew.backend.JoyCrewBackendApplication;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = JoyCrewBackendApplication.class)
@Transactional
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CompanyRepository companyRepository;

    private String testEmail = "integration_new@joycrew.com";
    private String testPassword = "newPass123!";
    private String testName = "새통합유저";
    private Company defaultCompany;
    private Employee registeredEmployee; // <-- setUp에서 등록된 Employee를 저장할 필드 추가

    @BeforeEach
    void setUp() {
        defaultCompany = Company.builder()
                .companyName("테스트컴퍼니2")
                .status("ACTIVE")
                .startAt(LocalDateTime.now())
                .totalCompanyBalance(0.0)
                .build();
        defaultCompany = companyRepository.save(defaultCompany);

        employeeRepository.findByEmail(testEmail).ifPresent(employeeRepository::delete);

        // --- setUp에서 Employee를 등록하고 필드에 저장 ---
        employeeService.registerEmployee(testEmail, testPassword, testName, defaultCompany);
        registeredEmployee = employeeRepository.findByEmail(testEmail).orElseThrow(); // 등록된 Employee 조회
        // --- 수정 끝 ---
    }

    @Test
    @DisplayName("통합 테스트: 직원 등록 성공 및 Wallet 자동 생성 확인")
    void registerEmployee_Integration_Success_And_WalletCreated() {
        // Given
        // 이 테스트는 새로운 직원을 등록하는 것이 아니라, setUp에서 등록된 직원의 상태를 확인하는 테스트로 변경
        // 또는, 새로운 이메일을 가진 직원을 등록하는 테스트로 변경
        String newTestEmailForSuccess = "success_test@joycrew.com";
        employeeRepository.findByEmail(newTestEmailForSuccess).ifPresent(employeeRepository::delete); // 혹시 모를 잔여 데이터 삭제

        // When
        employeeService.registerEmployee(newTestEmailForSuccess, "successPass123", "성공유저", defaultCompany);

        // Then
        Optional<Employee> savedEmployeeOptional = employeeRepository.findByEmail(newTestEmailForSuccess);
        assertThat(savedEmployeeOptional).isPresent();
        Employee savedEmployee = savedEmployeeOptional.get();

        assertThat(savedEmployee.getEmployeeName()).isEqualTo("성공유저");
        assertThat(passwordEncoder.matches("successPass123", savedEmployee.getPasswordHash())).isTrue();
        assertThat(savedEmployee.getRole()).isEqualTo(UserRole.EMPLOYEE);
        assertThat(savedEmployee.getStatus()).isEqualTo("ACTIVE");

        Optional<Wallet> savedWalletOptional = walletRepository.findByEmployee_EmployeeId(savedEmployee.getEmployeeId());
        assertThat(savedWalletOptional).isPresent();
        Wallet savedWallet = savedWalletOptional.get();

        assertThat(savedWallet.getEmployee().getEmployeeId()).isEqualTo(savedEmployee.getEmployeeId());
        assertThat(savedWallet.getBalance()).isEqualTo(0);
        assertThat(savedWallet.getGiftablePoint()).isEqualTo(0);
    }

    @Test
    @DisplayName("통합 테스트: 직원 등록 실패 - 이메일 중복")
    void registerEmployee_Integration_Failure_EmailDuplicate() {
        // Given
        // setUp에서 이미 testEmail로 직원이 등록되어 있음

        // When & Then
        // 동일한 이메일로 다시 등록 시도 시 예외 발생 확인
        assertThatThrownBy(() -> employeeService.registerEmployee(testEmail, "anotherPass", "다른이름", defaultCompany))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다.");
    }
}