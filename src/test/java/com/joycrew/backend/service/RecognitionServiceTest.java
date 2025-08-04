package com.joycrew.backend.service;

import com.joycrew.backend.dto.RecognitionRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.Tag;
import com.joycrew.backend.exception.InsufficientPointsException;
import com.joycrew.backend.exception.UserNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecognitionServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private RewardPointTransactionRepository transactionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RecognitionService recognitionService;

    private Employee sender;
    private Employee receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private RecognitionRequest request;

    @BeforeEach
    void setUp() {
        sender = Employee.builder().employeeId(1L).email("sender@joycrew.com").build();
        receiver = Employee.builder().employeeId(2L).email("receiver@joycrew.com").build();
        senderWallet = spy(new Wallet(sender));
        receiverWallet = spy(new Wallet(receiver));
        request = new RecognitionRequest(2L, 100, "Great job!", List.of(Tag.TEAMWORK));
    }

    @Test
    @DisplayName("[Service] 포인트 선물 성공")
    void sendRecognition_Success() {
        // Given
        senderWallet.addPoints(500);

        when(employeeRepository.findByEmail("sender@joycrew.com")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmployee_EmployeeId(2L)).thenReturn(Optional.of(receiverWallet));

        // When
        recognitionService.sendRecognition("sender@joycrew.com", request);

        // Then
        verify(senderWallet, times(1)).spendPoints(100);
        verify(receiverWallet, times(1)).addPoints(100);
        verify(transactionRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("[Service] 포인트 선물 실패 - 보내는 사용자를 찾을 수 없음")
    void sendRecognition_Failure_SenderNotFound() {
        // Given
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recognitionService.sendRecognition("sender@joycrew.com", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("[Service] 포인트 선물 실패 - 포인트 부족")
    void sendRecognition_Failure_InsufficientPoints() {
        // Given
        when(employeeRepository.findByEmail("sender@joycrew.com")).thenReturn(Optional.of(sender));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmployee_EmployeeId(2L)).thenReturn(Optional.of(receiverWallet));

        // When & Then
        assertThatThrownBy(() -> recognitionService.sendRecognition("sender@joycrew.com", request))
                .isInstanceOf(InsufficientPointsException.class);
    }
}