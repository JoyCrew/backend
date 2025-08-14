package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock
  private WalletRepository walletRepository;
  @Mock
  private EmployeeRepository employeeRepository;
  @Mock
  private EmployeeMapper employeeMapper;

  @InjectMocks
  private WalletService walletService;

  @Test
  @DisplayName("[Unit] Get point balance successfully")
  void getPointBalance_Success() {
    // Given
    String userEmail = "test@joycrew.com";
    Employee mockEmployee = Employee.builder().employeeId(1L).build();
    Wallet mockWallet = new Wallet(mockEmployee);
    mockWallet.addPoints(500);
    PointBalanceResponse mockDto = new PointBalanceResponse(500, 500);

    when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockEmployee));
    when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(mockWallet));
    when(employeeMapper.toPointBalanceResponse(any(Wallet.class))).thenReturn(mockDto); // Mock the mapper's behavior

    // When
    PointBalanceResponse response = walletService.getPointBalance(userEmail);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.totalBalance()).isEqualTo(500);
    assertThat(response.giftableBalance()).isEqualTo(500);
  }

  @Test
  @DisplayName("[Unit] Get point balance failure - User not found")
  void getPointBalance_Failure_UserNotFound() {
    // Given
    String userEmail = "notfound@joycrew.com";
    when(employeeRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> walletService.getPointBalance(userEmail))
        .isInstanceOf(UserNotFoundException.class);
  }
}