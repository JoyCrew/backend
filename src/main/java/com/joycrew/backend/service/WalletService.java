package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {
    private final WalletRepository walletRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional // ⭐️ Wallet 생성 및 저장 필요하므로 @Transactional 재정의
    public PointBalanceResponse getPointBalance(String userEmail) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));

        // ⭐️ orElseGet을 사용하여 Wallet이 없으면 생성 후 DB에 저장 (영속성 문제 해결)
        Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet(employee);
                    return walletRepository.save(newWallet);
                });

        return employeeMapper.toPointBalanceResponse(wallet);
    }
}