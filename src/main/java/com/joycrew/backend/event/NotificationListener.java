package com.joycrew.backend.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener {

  @Async
  @EventListener
  public void handleRecognitionEvent(RecognitionEvent event) {
    log.info("Recognition event received. Starting asynchronous processing.");
    try {
      // Simulate a delay for notification processing (e.g., sending a push notification).
      Thread.sleep(2000);
      log.info("User {} gifted {} points to user {}. Message: {}",
          event.getSenderId(), event.getPoints(), event.getReceiverId(), event.getMessage());
    } catch (InterruptedException e) {
      log.error("Error occurred while processing notification", e);
      Thread.currentThread().interrupt();
    }
  }
}