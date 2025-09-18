// com.joycrew.backend.service.NotificationService.java
package com.joycrew.backend.service;

import com.joycrew.backend.dto.NotificationResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Notification;
import com.joycrew.backend.entity.enums.NotificationType;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Notification createGiftNotification(Employee sender, Employee receiver, int points, String message) {
        String actorName  = sender != null ? sender.getEmployeeName() : null;
        String actorEmail = sender != null ? sender.getEmail() : null;

        String who = actorName != null && !actorName.isBlank()
                ? actorName
                : (actorEmail != null ? actorEmail : "동료");

        Notification n = Notification.builder()
                .recipient(receiver)
                .actor(sender)
                .type(NotificationType.GIFT_RECEIVED)
                .title("포인트 선물 도착")
                .content(who + "님이 " + points + "P를 보냈어요" +
                        ((message != null && !message.isBlank()) ? (": " + message) : ""))
                .pointAmount(points)
                .read(false)
                .build();

        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadForToast(String userEmail) {
        Employee emp = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return notificationRepository
                .findTop50ByRecipientAndReadFalseAndExpiresAtAfterOrderByCreatedAtDesc(emp, LocalDateTime.now())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentSince(String userEmail, int hours) {
        Employee emp = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return notificationRepository
                .findTop100ByRecipientAndCreatedAtAfterOrderByCreatedAtDesc(emp, LocalDateTime.now().minusHours(hours))
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void markRead(Long notifId, String userEmail) {
        Employee emp = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        Notification n = notificationRepository.findById(notifId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));
        if (!n.getRecipient().getEmployeeId().equals(emp.getEmployeeId()))
            throw new IllegalStateException("Forbidden.");
        n.markRead();
    }

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .pointAmount(n.getPointAmount())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .expiresAt(n.getExpiresAt())
                .actorId(n.getActor() != null ? n.getActor().getEmployeeId() : null)
                .actorEmail(n.getActor() != null ? n.getActor().getEmail() : null)
                .actorName(n.getActor() != null ? n.getActor().getEmployeeName() : null)  // 👈 이름 사용
                .recipientId(n.getRecipient() != null ? n.getRecipient().getEmployeeId() : null)
                .build();
    }
}
