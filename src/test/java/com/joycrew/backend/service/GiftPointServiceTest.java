package com.joycrew.backend.service;

import com.joycrew.backend.dto.GiftPointRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.InsufficientPointsException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftPointServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private RewardPointTransactionRepository transactionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private GiftPointService giftPointService;

    private Employee sender, receiver;
    private Wallet senderWallet, receiverWallet;

    @BeforeEach
    void setUp() {
        sender = Employee.builder().employeeId(1L).build();
        receiver = Employee.builder().employeeId(2L).build();
        senderWallet = new Wallet(sender);
        receiverWallet = new Wallet(receiver);
    }

    @Test
    @DisplayName("[Unit] Gift points successfully")
    void giftPoints_Success() {
        // Given
        senderWallet.addPoints(100);
        GiftPointRequest request = new GiftPointRequest(2L, 50, "Thanks!", List.of());
        when(employeeRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmployee_EmployeeId(2L)).thenReturn(Optional.of(receiverWallet));

        // When
        giftPointService.giftPointsToColleague("sender@test.com", request);

        // Then
        verify(transactionRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any());
        assertThat(senderWallet.getBalance()).isEqualTo(50);
        assertThat(receiverWallet.getBalance()).isEqualTo(50);
    }

    @Test
    @DisplayName("[Unit] Gift points failure - Insufficient points")
    void giftPoints_Failure_InsufficientPoints() {
        // Given
        GiftPointRequest request = new GiftPointRequest(2L, 50, "Thanks!", List.of());
        when(employeeRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmployee_EmployeeId(2L)).thenReturn(Optional.of(receiverWallet));

        // When & Then
        assertThatThrownBy(() -> giftPointService.giftPointsToColleague("sender@test.com", request))
                .isInstanceOf(InsufficientPointsException.class);
    }
}