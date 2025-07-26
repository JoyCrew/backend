package com.joycrew.backend.service;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final WalletRepository walletRepository; // Wallet 정보 조회를 위해 주입
    private final PasswordEncoder passwordEncoder;

    /**
     * [리팩토링]
     * 사용자 프로필 조회 로직을 서비스 계층으로 이동.
     * 컨트롤러는 이 메서드를 호출하여 DTO를 받기만 하면 됨.
     * @param userEmail 조회할 사용자의 이메일
     * @return UserProfileResponse DTO
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userEmail) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));

        // Wallet 정보 조회 로직도 서비스 계층에서 처리
        Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                .orElse(new Wallet(employee));

        return UserProfileResponse.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getEmployeeName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .position(employee.getPosition())
                .totalBalance(wallet.getBalance())
                .giftableBalance(wallet.getGiftablePoint())
                .build();
    }

    /**
     * [직원 기능] 첫 로그인 시 비밀번호를 변경합니다.
     * @param userEmail 현재 로그인된 사용자 이메일
     * @param request   새 비밀번호 정보 DTO
     */
    public void forcePasswordChange(String userEmail, PasswordChangeRequest request) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));
        employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
    }
}