package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {
    private final WalletRepository walletRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * [리팩토링]
     * 포인트 잔액 조회 로직을 별도의 WalletService로 분리하여 응집도를 높임.
     * @param userEmail 조회할 사용자의 이메일
     * @return PointBalanceResponse DTO
     */
    public PointBalanceResponse getPointBalance(String userEmail) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));

        Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                .orElse(new Wallet(employee));

        return new PointBalanceResponse(wallet.getBalance(), wallet.getGiftablePoint());
    }
}
