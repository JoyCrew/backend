package com.joycrew.backend.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordHashPrinter {

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void print() {
        String raw = "1234";
        String hash = passwordEncoder.encode(raw);
        System.out.println("📌 실제 해시: " + hash);
    }
}
