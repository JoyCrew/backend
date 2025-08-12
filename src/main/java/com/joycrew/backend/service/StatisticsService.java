package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointStatisticsResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.enums.Tag;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<RewardPointTransaction> transactions = transactionRepository.findBySenderOrReceiver(user, user);

        int totalReceived = transactions.stream()
                .filter(tx -> user.equals(tx.getReceiver()))
                .mapToInt(RewardPointTransaction::getPointAmount)
                .sum();

        int totalSent = transactions.stream()
                .filter(tx -> user.equals(tx.getSender()))
                .mapToInt(RewardPointTransaction::getPointAmount)
                .sum();

        Map<Tag, Long> tagStats = transactions.stream()
                .filter(tx -> user.equals(tx.getReceiver()) && tx.getTags() != null)
                .flatMap(tx -> tx.getTags().stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        return new PointStatisticsResponse(totalReceived, totalSent, tagStats);
    }
}