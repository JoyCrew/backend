package com.joycrew.backend.service;

import com.joycrew.backend.dto.GiftPointRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.event.RecognitionEvent;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.RewardPointTransactionRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GiftPointService {

    private final EmployeeRepository employeeRepository;
    private final WalletRepository walletRepository;
    private final RewardPointTransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void giftPointsToColleague(String senderEmail, GiftPointRequest request) {
        Employee sender = employeeRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserNotFoundException("Sender not found."));
        Employee receiver = employeeRepository.findById(request.receiverId())
                .orElseThrow(() -> new UserNotFoundException("Receiver not found."));

        Wallet senderWallet = walletRepository.findByEmployee_EmployeeId(sender.getEmployeeId())
                .orElseThrow(() -> new IllegalStateException("Sender's wallet does not exist."));
        Wallet receiverWallet = walletRepository.findByEmployee_EmployeeId(receiver.getEmployeeId())
                .orElseThrow(() -> new IllegalStateException("Receiver's wallet does not exist."));

        // Transfer points
        // 1. 보내는 사람: '총 잔액(balance)'과 '선물 한도(giftablePoint)' 둘 다 차감 (기존 로직)
        senderWallet.spendGiftablePoints(request.points());

        // [FIXED] 2. 받는 사람: '총 잔액(balance)'만 증가시킴
        receiverWallet.receiveGiftPoints(request.points());
        // receiverWallet.addPoints(request.points()); // 👈 기존 코드

        // Record the transaction
        RewardPointTransaction transaction = RewardPointTransaction.builder()
                .sender(sender)
                .receiver(receiver)
                .pointAmount(request.points())
                .message(request.message())
                .type(TransactionType.AWARD_P2P)
                .tags(request.tags())
                .build();
        transactionRepository.save(transaction);

        // Publish an event for notifications or other async tasks
        eventPublisher.publishEvent(
                new RecognitionEvent(this, sender.getEmployeeId(), receiver.getEmployeeId(), request.points(), request.message())
        );
    }
}