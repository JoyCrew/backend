package com.joycrew.backend.service;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.event.RecognitionEvent;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecognitionService {
    private final EmployeeRepository employeeRepository;
    private final WalletRepository walletRepository;
    private final RewardPointTransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher; // [L3] 이벤트 발행을 위한 주입

    @Transactional
    public void sendRecognition(String senderEmail, RecognitionRequest request) {
        Employee sender = employeeRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserNotFoundException("보내는 사용자를 찾을 수 없습니다."));
        Employee receiver = employeeRepository.findById(request.receiverId())
                .orElseThrow(() -> new UserNotFoundException("받는 사용자를 찾을 수 없습니다."));

        Wallet senderWallet = walletRepository.findByEmployee_EmployeeId(sender.getEmployeeId())
                .orElseThrow(() -> new IllegalStateException("보내는 사용자의 지갑이 없습니다."));
        Wallet receiverWallet = walletRepository.findByEmployee_EmployeeId(receiver.getEmployeeId())
                .orElseThrow(() -> new IllegalStateException("받는 사용자의 지갑이 없습니다."));

        senderWallet.spendPoints(request.points());
        receiverWallet.addPoints(request.points());

        RewardPointTransaction transaction = RewardPointTransaction.builder()
                .sender(sender)
                .receiver(receiver)
                .pointAmount(request.points())
                .message(request.message())
                .type(TransactionType.AWARD_P2P)
                .build();
        transactionRepository.save(transaction);

        eventPublisher.publishEvent(new RecognitionEvent(this, sender.getEmployeeId(), receiver.getEmployeeId(), request.points(), request.message()));
    }
}