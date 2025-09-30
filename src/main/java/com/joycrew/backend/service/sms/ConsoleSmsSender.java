package com.joycrew.backend.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("consoleSmsSender")
public class ConsoleSmsSender implements SmsSender {
    @Override
    public void send(String toPhone, String message) {
        log.info("[SMS:CONSOLE] to={}, body={}", toPhone, message);
    }
}