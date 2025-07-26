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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EmployeeRepository employeeRepository;

    private Company testCompany;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // 1. 의존하는 엔티티 먼저 생성 및 영속화
        testCompany = Company.builder().companyName("테스트회사").build();
        entityManager.persist(testCompany);

        testDepartment = Department.builder().name("테스트부서").company(testCompany).build();
        entityManager.persist(testDepartment);

        // 2. 테스트 대상 주체인 Employee 생성
        Employee testEmployee = Employee.builder()
                .company(testCompany)
                .department(testDepartment)
                .email("test@joycrew.com")
                .passwordHash("encodedPassword")
                .employeeName("김테스트")
                .position("사원")
                .role(UserRole.EMPLOYEE)
                .build();
        entityManager.persist(testEmployee);

        // 3. [수정] Wallet은 이제 new 키워드와 생성자를 통해서만 생성
        Wallet testWallet = new Wallet(testEmployee);
        entityManager.persist(testWallet);

        // 4. DB에 변경사항을 반영하고, 영속성 컨텍스트를 비워 순수한 DB 조회 테스트 환경 구축
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("이메일로 직원 조회 성공 (@EntityGraph 적용 확인)")
    void findByEmail_Success() {
        // When
        Optional<Employee> foundEmployee = employeeRepository.findByEmail("test@joycrew.com");

        // Then
        assertThat(foundEmployee).isPresent();
        assertThat(foundEmployee.get().getEmail()).isEqualTo("test@joycrew.com");
        // @EntityGraph 덕분에 추가 쿼리 없이 연관 엔티티 접근 가능
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
}
