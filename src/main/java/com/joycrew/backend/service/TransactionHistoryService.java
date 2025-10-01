package com.joycrew.backend.service;

import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionHistoryService {

    private final RewardPointTransactionRepository transactionRepository;
    private final EmployeeRepository employeeRepository;

    public List<TransactionHistoryResponse> getTransactionHistory(String userEmail) {
        Employee user = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        List<TransactionType> personalTransactionTypes = List.of(
                TransactionType.AWARD_P2P
        );

        return transactionRepository.findBySenderOrReceiverOrderByTransactionDateDesc(user, user)
                .stream()
                .filter(tx -> personalTransactionTypes.contains(tx.getType()))
                .map(tx -> {
                    boolean isSender = user.equals(tx.getSender());
                    int amount = isSender ? -tx.getPointAmount() : tx.getPointAmount();

                    String counterparty = "System/Admin"; // 기본값 설정
                    Employee counterpartyEmployee = null;

                    if (isSender) {
                        counterpartyEmployee = tx.getReceiver();
                    } else {
                        counterpartyEmployee = tx.getSender();
                    }

                    if (counterpartyEmployee != null) {
                        counterparty = counterpartyEmployee.getEmployeeName();
                        // ⭐️ 여기에서 counterpartyProfileImageUrl, counterpartyDepartmentName도 매핑해야 함
                    } else if (tx.getType() == TransactionType.AWARD_MANAGER_SPOT) {
                        counterparty = "Admin";
                    } else if (tx.getType() == TransactionType.REDEEM_ITEM || tx.getType() == TransactionType.EXPIRE_POINTS) {
                        counterparty = "System";
                    }

                    return TransactionHistoryResponse.builder()
                            .transactionId(tx.getTransactionId())
                            .type(tx.getType())
                            .amount(amount)
                            .counterparty(counterparty)
                            .message(tx.getMessage())
                            .transactionDate(tx.getTransactionDate())
                            .build();
                })
                .collect(Collectors.toList());
    }
}