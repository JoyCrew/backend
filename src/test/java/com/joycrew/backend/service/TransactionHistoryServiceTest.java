package com.joycrew.backend.service;

import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceTest {

    @Mock
    private RewardPointTransactionRepository transactionRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TransactionHistoryService transactionHistoryService;

    @Test
    @DisplayName("[Service] 포인트 거래 내역 조회 성공")
    void getTransactionHistory_Success() {
        // Given
        String userEmail = "user@joycrew.com";
        Employee user = Employee.builder().employeeId(1L).employeeName("테스트유저").email(userEmail).build();
        Employee colleague = Employee.builder().employeeId(2L).employeeName("동료").email("colleague@joycrew.com").build();

        RewardPointTransaction sentTx = RewardPointTransaction.builder()
                .transactionId(101L).sender(user).receiver(colleague)
                .pointAmount(50).type(TransactionType.AWARD_P2P)
                .transactionDate(LocalDateTime.now()).build();

        RewardPointTransaction receivedTx = RewardPointTransaction.builder()
                .transactionId(102L).sender(colleague).receiver(user)
                .pointAmount(100).type(TransactionType.AWARD_P2P)
                .transactionDate(LocalDateTime.now().minusDays(1)).build();

        when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(transactionRepository.findBySenderOrReceiverOrderByTransactionDateDesc(user, user))
                .thenReturn(List.of(sentTx, receivedTx));

        // When
        List<TransactionHistoryResponse> history = transactionHistoryService.getTransactionHistory(userEmail);

        // Then
        assertThat(history).hasSize(2);

        TransactionHistoryResponse sentResponse = history.get(0);
        assertThat(sentResponse.amount()).isEqualTo(-50);
        assertThat(sentResponse.counterparty()).isEqualTo("동료");

        TransactionHistoryResponse receivedResponse = history.get(1);
        assertThat(receivedResponse.amount()).isEqualTo(100);
        assertThat(receivedResponse.counterparty()).isEqualTo("동료");
    }
}