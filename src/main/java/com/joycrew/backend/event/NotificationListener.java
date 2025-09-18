package com.joycrew.backend.event;

import com.joycrew.backend.dto.NotificationResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Notification;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.service.NotificationService;
import com.joycrew.backend.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

  private final NotificationService notificationService;
  private final NotificationSseService sseService;
  private final EmployeeRepository employeeRepository;

  @Async
  @EventListener
  public void handleRecognitionEvent(RecognitionEvent event) {
    try {
      Long senderId = event.getSenderId();
      Long receiverId = event.getReceiverId();

      Employee sender = (senderId != null) ? employeeRepository.findById(senderId).orElse(null) : null;
      Employee receiver = (receiverId != null) ? employeeRepository.findById(receiverId).orElse(null) : null;
      if (receiver == null) {
        log.warn("RecognitionEvent: receiver not found (receiverId={})", receiverId);
        return;
      }

      // 1) 알림 저장(DB)
      Notification saved = notificationService.createGiftNotification(
              sender, receiver, event.getPoints(), event.getMessage());

      // 2) DTO(payload) 구성 (record + @Builder)
      NotificationResponse payload = NotificationResponse.builder()
              .id(saved.getId())
              .type(saved.getType())
              .title(saved.getTitle())
              .content(saved.getContent())
              .pointAmount(saved.getPointAmount())
              .read(saved.isRead())
              .createdAt(saved.getCreatedAt())
              .expiresAt(saved.getExpiresAt())
              .actorId(saved.getActor() != null ? saved.getActor().getEmployeeId() : null)
              .actorEmail(saved.getActor() != null ? saved.getActor().getEmail() : null)
              .actorName(saved.getActor() != null ? saved.getActor().getEmployeeName() : null)
              .recipientId(saved.getRecipient() != null ? saved.getRecipient().getEmployeeId() : null)
              .build();

      // 3) 수신자 쪽 앱 알림 OFF면 푸시 생략(옵션)
      if (Boolean.FALSE.equals(receiver.getAppNotificationEnabled())) {
        log.info("Notifications disabled for user={}, skip SSE push", receiver.getEmail());
        return;
      }

      // 4) 실시간 SSE 푸시 (채널: 수신자 이메일)
      sseService.push(receiver.getEmail(), payload);
      log.info("Pushed gift notification to {}", receiver.getEmail());

    } catch (Exception e) {
      log.error("Notification handling failed", e);
    }
  }
}
