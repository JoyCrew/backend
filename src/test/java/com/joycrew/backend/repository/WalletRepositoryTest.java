package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private WalletRepository walletRepository;

    private Employee testEmployeeWithWallet;
    private Employee testEmployeeWithoutWallet;

    @BeforeEach
    void setUp() {
        Company testCompany = Company.builder().companyName("테스트회사").build();
        entityManager.persist(testCompany);

        testEmployeeWithWallet = Employee.builder()
                .company(testCompany)
                .email("walletuser@joycrew.com")
                .passwordHash("pass123")
                .employeeName("지갑유저")
                .role(UserRole.EMPLOYEE)
                .build();
        entityManager.persist(testEmployeeWithWallet);

        testEmployeeWithoutWallet = Employee.builder()
                .company(testCompany)
                .email("nowallet@joycrew.com")
                .passwordHash("pass123")
                .employeeName("지갑없는유저")
                .role(UserRole.EMPLOYEE)
                .build();
        entityManager.persist(testEmployeeWithoutWallet);

        // [수정] Wallet은 이제 new 키워드와 생성자를 통해서만 생성
        Wallet testWallet = new Wallet(testEmployeeWithWallet);
        // [수정] 도메인 메서드를 사용하여 상태 변경 (Setter 대신)
        testWallet.addPoints(5000);
        entityManager.persist(testWallet);

        // [수정] employee.setWallet()은 불가능하며, 불필요하므로 제거.
        // Wallet이 Employee의 참조를 가지고 있으므로 관계는 이미 설정됨.

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
    }

    @Test
    @DisplayName("Employee ID로 Wallet 조회 실패 - Wallet 없음")
    void findByEmployee_EmployeeId_NotFound() {
        // When
        Optional<Wallet> foundWallet = walletRepository.findByEmployee_EmployeeId(testEmployeeWithoutWallet.getEmployeeId());

        // Then
        assertThat(foundWallet).isEmpty();
    }
}
