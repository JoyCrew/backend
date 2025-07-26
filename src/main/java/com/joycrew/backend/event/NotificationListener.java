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
        log.info("포인트 전송 이벤트 수신 (비동기 처리 시작)");
        try {
            Thread.sleep(2000);
            log.info("{}님이 {}님에게 {} 포인트를 선물했습니다. 메시지: {}",
                    event.getSenderId(), event.getReceiverId(), event.getPoints(), event.getMessage());
        } catch (InterruptedException e) {
            log.error("알림 처리 중 오류 발생", e);
            Thread.currentThread().interrupt();
        }
    }
