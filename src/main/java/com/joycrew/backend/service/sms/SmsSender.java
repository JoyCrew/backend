package com.joycrew.backend.service.sms;

public interface SmsSender {
    void send(String toPhone, String message);
}