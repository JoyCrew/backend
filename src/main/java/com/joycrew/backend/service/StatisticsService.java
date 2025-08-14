package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointStatisticsResponse;
import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.enums.Tag;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

  private final EmployeeRepository employeeRepository;
  private final RewardPointTransactionRepository transactionRepository;

  public PointStatisticsResponse getPointStatistics(String userEmail) {
    Employee user = employeeRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

    // Use a query that fetches related entities to avoid N+1 problems
    List<RewardPointTransaction> allTransactions = transactionRepository.findBySenderOrReceiverOrderByTransactionDateDesc(user, user);

    List<TransactionHistoryResponse> receivedHistory = new ArrayList<>();
    List<TransactionHistoryResponse> sentHistory = new ArrayList<>();

    for (RewardPointTransaction tx : allTransactions) {
      if (user.equals(tx.getReceiver())) {
        Employee sender = tx.getSender();
        receivedHistory.add(TransactionHistoryResponse.builder()
                .transactionId(tx.getTransactionId())
                .type(tx.getType())
                .amount(tx.getPointAmount())
                .counterparty(sender != null ? sender.getEmployeeName() : "System")
                .message(tx.getMessage())
                .transactionDate(tx.getTransactionDate())
                .counterpartyProfileImageUrl(sender != null ? sender.getProfileImageUrl() : null)
                .counterpartyDepartmentName(sender != null && sender.getDepartment() != null ? sender.getDepartment().getName() : null)
                .build());
      } else if (user.equals(tx.getSender())) {
        Employee receiver = tx.getReceiver();
        sentHistory.add(TransactionHistoryResponse.builder()
                .transactionId(tx.getTransactionId())
                .type(tx.getType())
                .amount(-tx.getPointAmount()) // Sent points are negative
                .counterparty(receiver != null ? receiver.getEmployeeName() : "System")
                .message(tx.getMessage())
                .transactionDate(tx.getTransactionDate())
                .counterpartyProfileImageUrl(receiver != null ? receiver.getProfileImageUrl() : null)
                .counterpartyDepartmentName(receiver != null && receiver.getDepartment() != null ? receiver.getDepartment().getName() : null)
                .build());
      }
    }

    int totalReceived = receivedHistory.stream().mapToInt(TransactionHistoryResponse::amount).sum();
    int totalSent = sentHistory.stream().mapToInt(TransactionHistoryResponse::amount).sum();

    Map<Tag, Long> tagStatsMap = allTransactions.stream()
            .filter(tx -> user.equals(tx.getReceiver()) && tx.getTags() != null)
            .flatMap(tx -> tx.getTags().stream())
            .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

    List<Long> sortedTagCounts = Arrays.stream(Tag.values())
            .map(tag -> tagStatsMap.getOrDefault(tag, 0L))
            .collect(Collectors.toList());

    return new PointStatisticsResponse(totalReceived, Math.abs(totalSent), sortedTagCounts, receivedHistory, sentHistory);
  }
}