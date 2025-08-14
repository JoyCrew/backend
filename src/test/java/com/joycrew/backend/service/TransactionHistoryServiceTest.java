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
  @DisplayName("[Unit] Get transaction history - Should include P2P and item purchases, but exclude admin awards")
  void getTransactionHistory_Success_WithFiltering() {
    // Given
    String userEmail = "user@joycrew.com";
    Employee user = Employee.builder().employeeId(1L).employeeName("Test User").email(userEmail).build();
    Employee colleague = Employee.builder().employeeId(2L).employeeName("Colleague").build();
    Employee admin = Employee.builder().employeeId(99L).employeeName("Admin").build();

    RewardPointTransaction p2pSentTx = RewardPointTransaction.builder()
        .transactionId(101L).sender(user).receiver(colleague)
        .pointAmount(50).type(TransactionType.AWARD_P2P)
        .transactionDate(LocalDateTime.now().minusDays(1)).build();

    RewardPointTransaction itemRedeemTx = RewardPointTransaction.builder()
        .transactionId(102L).sender(user).receiver(null) // receiver is null for item purchases
        .pointAmount(200).type(TransactionType.REDEEM_ITEM).message("Purchased: Coffee")
        .transactionDate(LocalDateTime.now()).build();

    // This transaction should be filtered out
    RewardPointTransaction adminAwardTx = RewardPointTransaction.builder()
        .transactionId(103L).sender(admin).receiver(user)
        .pointAmount(1000).type(TransactionType.AWARD_MANAGER_SPOT)
        .transactionDate(LocalDateTime.now().minusDays(2)).build();

    when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
    when(transactionRepository.findBySenderOrReceiverOrderByTransactionDateDesc(user, user))
        .thenReturn(List.of(itemRedeemTx, p2pSentTx, adminAwardTx));

    // When
    List<TransactionHistoryResponse> history = transactionHistoryService.getTransactionHistory(userEmail);

    // Then
    assertThat(history).hasSize(2);
    assertThat(history.stream().map(TransactionHistoryResponse::transactionId))
        .containsExactlyInAnyOrder(101L, 102L)
        .doesNotContain(103L);

    TransactionHistoryResponse redeemResponse = history.stream().filter(h -> h.type() == TransactionType.REDEEM_ITEM).findFirst().get();
    assertThat(redeemResponse.amount()).isEqualTo(-200);
    assertThat(redeemResponse.counterparty()).isEqualTo("Purchased: Coffee");

    TransactionHistoryResponse p2pResponse = history.stream().filter(h -> h.type() == TransactionType.AWARD_P2P).findFirst().get();
    assertThat(p2pResponse.amount()).isEqualTo(-50);
    assertThat(p2pResponse.counterparty()).isEqualTo("Colleague");
  }
}