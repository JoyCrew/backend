package com.joycrew.backend.auth;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {

    private final EmployeeRepository employeeRepository;

    /**
     * PASS 복호화 결과에서 받은 신원정보로 로그인 아이디(사내에선 보통 email)를 조회.
     * - CI를 쓰고 싶다면 Employee 엔티티에 ci 필드 추가 후 findByCi 사용 권장.
     */
    public Optional<String> findLoginIdByIdentity(String name, LocalDate birthday, String mobileNo) {
        // 가장 보편: 휴대폰 번호로 1차 후보 → 이름/생일 2차 검증
        Optional<Employee> byPhone = employeeRepository.findByPhoneNumber(mobileNo);
        return byPhone
                .filter(e -> (e.getEmployeeName() == null || e.getEmployeeName().equals(name)))
                .filter(e -> (e.getBirthday() == null || e.getBirthday().equals(birthday)))
                .map(Employee::getEmail); // 시스템에서 '아이디'가 이메일일 경우
    }
}
