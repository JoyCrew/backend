package com.joycrew.backend.service;

import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.entity.Employee;
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

        // This call now efficiently fetches transactions with related sender/receiver data.
        return transactionRepository.findBySenderOrReceiverOrderByTransactionDateDesc(user, user)
                .stream()
                .map(tx -> {
                    boolean isSender = user.equals(tx.getSender());
                    int amount = isSender ? -tx.getPointAmount() : tx.getPointAmount();

                    String counterparty;
                    switch (tx.getType()) {
                        case REDEEM_ITEM:
                            counterparty = tx.getMessage() != null ? tx.getMessage() : "상품 구매";
                            break;
                        case AWARD_P2P:
                        case AWARD_MANAGER_SPOT:
                        case ADMIN_ADJUSTMENT:
                            counterparty = isSender
                                    ? tx.getReceiver().getEmployeeName()
                                    : (tx.getSender() != null ? tx.getSender().getEmployeeName() : "System");
                            break;
                        case AWARD_AUTOMATED:
                        case EXPIRE_POINTS:
                        default:
                            counterparty = "System";
                            break;
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