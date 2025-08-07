package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminPointDistributionRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPointService {

    private final EmployeeRepository employeeRepository;
    private final WalletRepository walletRepository;
    private final RewardPointTransactionRepository transactionRepository;

    public void distributePoints(AdminPointDistributionRequest request, Employee admin) {
        List<Employee> employees = employeeRepository.findAllById(request.employeeIds());
        if (employees.size() != request.employeeIds().size()) {
            throw new UserNotFoundException("Could not find some of the requested employees. Please verify the IDs.");
        }

        for (Employee employee : employees) {
            Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                    .orElseThrow(() -> new IllegalStateException("Wallet not found for employee: " + employee.getEmployeeName()));

            if (request.points() > 0) {
                wallet.addPoints(request.points());
            } else {
                wallet.spendPoints(Math.abs(request.points()));
            }

            RewardPointTransaction transaction = RewardPointTransaction.builder()
                    .sender(admin)
                    .receiver(employee)
                    .pointAmount(request.points())
                    .message(request.message())
                    .type(request.type())
                    .build();
            transactionRepository.save(transaction);
        }
    }
}