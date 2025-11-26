package com.joycrew.backend.service;

import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.tenant.Tenant;
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
        Long tenant = Tenant.id();

        Employee me = employeeRepository
                .findByCompanyCompanyIdAndEmail(tenant, userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        List<TransactionType> personalTypes = List.of(TransactionType.AWARD_P2P);

        return transactionRepository.findAllByCompanyScope(tenant).stream()
                .filter(tx -> {
                    // 본인 관련(보낸/받은)만 추려내기
                    Long senderId   = (tx.getSender()   != null) ? tx.getSender().getEmployeeId()   : null;
                    Long receiverId = (tx.getReceiver() != null) ? tx.getReceiver().getEmployeeId() : null;
                    return (senderId != null && senderId.equals(me.getEmployeeId()))
                            || (receiverId != null && receiverId.equals(me.getEmployeeId()));
                })
                .filter(tx -> personalTypes.contains(tx.getType()))
                .map(tx -> {
                    boolean isSender = me.equals(tx.getSender());
                    int amount = isSender ? -tx.getPointAmount() : tx.getPointAmount();

                    String counterparty = "System/Admin";
                    Employee cp = isSender ? tx.getReceiver() : tx.getSender();

                    if (cp != null) {
                        counterparty = cp.getEmployeeName();
                        // TODO: 필요 시 counterparty 이미지/부서명 등 추가 매핑
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
