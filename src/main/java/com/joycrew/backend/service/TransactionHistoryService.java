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

    // Define which transaction types are considered personal history
    List<TransactionType> personalTransactionTypes = List.of(
        TransactionType.AWARD_P2P,
        TransactionType.REDEEM_ITEM
    );

    return transactionRepository.findBySenderOrReceiverOrderByTransactionDateDesc(user, user)
        .stream()
        // Filter only for personal transaction types
        .filter(tx -> personalTransactionTypes.contains(tx.getType()))
        .map(tx -> {
          boolean isSender = user.equals(tx.getSender());
          // For item redemption, the user is the sender of points, so amount should be negative.
          int amount = isSender ? -tx.getPointAmount() : tx.getPointAmount();

          String counterparty;
          if (tx.getType() == TransactionType.REDEEM_ITEM) {
            counterparty = tx.getMessage() != null ? tx.getMessage() : "상품 구매";
          } else { // AWARD_P2P
            counterparty = isSender
                ? tx.getReceiver().getEmployeeName()
                : tx.getSender().getEmployeeName();
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