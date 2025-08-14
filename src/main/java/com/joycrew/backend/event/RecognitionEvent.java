package com.joycrew.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RecognitionEvent extends ApplicationEvent {
  private final Long senderId;
  private final Long receiverId;
  private final int points;
  private final String message;

  public RecognitionEvent(Object source, Long senderId, Long receiverId, int points, String message) {
    super(source);
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.points = points;
    this.message = message;
  }
}