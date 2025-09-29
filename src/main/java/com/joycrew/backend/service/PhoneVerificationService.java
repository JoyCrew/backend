package com.joycrew.backend.service;

import com.joycrew.backend.entity.PhoneVerification;
import com.joycrew.backend.repository.PhoneVerificationRepository;
import com.joycrew.backend.service.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationRepository repo;
    private final @Qualifier("smsSender") SmsSender sms;
    private final KycTokenService kycTokenService;

    @Value("${otp.ttl-minutes:5}") private int ttlMin;
    @Value("${otp.max-attempts:5}") private int maxAttempts;
    @Value("${otp.resend-cooldown-seconds:30}") private int cooldownSec;

    private final Random random = new Random();

    public StartResult start(String phone) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        String hash = BCrypt.hashpw(code, BCrypt.gensalt());
        var now = LocalDateTime.now();
        var pv = PhoneVerification.builder()
                .phone(phone).codeHash(hash)
                .expiresAt(now.plusMinutes(ttlMin))
                .attempts(0).maxAttempts(maxAttempts)
                .createdAt(now).lastSentAt(now)
                .requestId(UUID.randomUUID().toString())
                .status(PhoneVerification.Status.PENDING)
                .build();
        repo.save(pv);

        sms.send(phone, "[JoyCrew] 인증번호: " + code + " (유효 " + ttlMin + "분)");
        return new StartResult(pv.getRequestId(), cooldownSec);
    }

    public VerifyResult verify(String requestId, String code) {
        var pv = repo.findByRequestId(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "request not found"));

        if (pv.getStatus() != PhoneVerification.Status.PENDING)
            throw new ResponseStatusException(BAD_REQUEST, "already used");

        var now = LocalDateTime.now();
        if (now.isAfter(pv.getExpiresAt())) {
            pv.setStatus(PhoneVerification.Status.EXPIRED);
            repo.save(pv);
            throw new ResponseStatusException(BAD_REQUEST, "expired");
        }
        if (pv.getAttempts() >= pv.getMaxAttempts()) {
            pv.setStatus(PhoneVerification.Status.BLOCKED);
            repo.save(pv);
            throw new ResponseStatusException(TOO_MANY_REQUESTS, "too many attempts");
        }

        pv.setAttempts(pv.getAttempts() + 1);
        boolean ok = BCrypt.checkpw(code, pv.getCodeHash());
        if (!ok) {
            repo.save(pv);
            throw new ResponseStatusException(UNAUTHORIZED, "invalid code");
        }

        pv.setStatus(PhoneVerification.Status.VERIFIED);
        repo.save(pv);

        String token = kycTokenService.create(pv.getPhone());
        return new VerifyResult(true, token);
    }

    public record StartResult(String requestId, int resendAvailableInSec) {}
    public record VerifyResult(boolean verified, String kycToken) {}
}