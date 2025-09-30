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

/**
 * 휴대폰 OTP 발송/검증 서비스.
 * - start(phone): 6자리 코드 생성 후 bcrypt로 저장, SMS 발송, requestId 반환
 * - verify(requestId, code): 코드 검증, 상태 갱신, 시도 제한/만료 처리, KYC 토큰 발급
 *
 * 주의:
 * - resend 쿨다운은 현재 "반환"만 하고 실제 차단 로직은 넣지 않음(필요 시 조회/검증 추가).
 */
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationRepository repo;

    // SmsConfig에서 @Primary로 선택된 Sender 주입 (console 또는 solapi)
    private final @Qualifier("smsSender") SmsSender sms;

    private final KycTokenService kycTokenService;

    @Value("${otp.ttl-minutes:5}")
    private int ttlMin;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:30}")
    private int cooldownSec;

    private final Random random = new Random();

    /**
     * 인증 시작: 6자리 코드 생성 → 저장 → SMS 발송 → requestId 반환
     */
    public StartResult start(String phone) {
        // 6자리 랜덤 코드 생성
        String code = String.format("%06d", random.nextInt(1_000_000));

        // 코드 해시(bcrypt)
        String hash = BCrypt.hashpw(code, BCrypt.gensalt());

        var now = LocalDateTime.now();

        var pv = PhoneVerification.builder()
                .phone(phone)
                .codeHash(hash)
                .expiresAt(now.plusMinutes(ttlMin))
                .attempts(0)
                .maxAttempts(maxAttempts)
                .createdAt(now)
                .lastSentAt(now)
                .requestId(UUID.randomUUID().toString())
                .status(PhoneVerification.Status.PENDING)
                .build();

        repo.save(pv);

        // 실제 전송: console 모드면 로그로, solapi면 문자 발송
        sms.send(phone, "[JoyCrew] 인증번호: " + code + " (유효 " + ttlMin + "분)");

        // 현재 구현은 쿨다운을 "반환"만 함. (차단하고 싶으면 최근 전송 이력 조회/검증 추가)
        return new StartResult(pv.getRequestId(), cooldownSec);
    }

    /**
     * 코드 검증: 상태/만료/시도수 체크 → 성공 시 VERIFIED로 갱신하고 KYC 토큰 발급
     */
    public VerifyResult verify(String requestId, String code) {
        var pv = repo.findByRequestId(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "request not found"));

        if (pv.getStatus() != PhoneVerification.Status.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST, "already used");
        }

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

        // 시도 1 증가
        pv.setAttempts(pv.getAttempts() + 1);

        boolean ok = BCrypt.checkpw(code, pv.getCodeHash());
        if (!ok) {
            repo.save(pv);
            throw new ResponseStatusException(UNAUTHORIZED, "invalid code");
        }

        // 성공: VERIFIED 처리
        pv.setStatus(PhoneVerification.Status.VERIFIED);
        repo.save(pv);

        // KYC 토큰 발급 (payload: phone | issuedAt | exp | sig)
        String token = kycTokenService.create(pv.getPhone());

        // 컨트롤러에서 이메일/최근로그인 조회가 필요하므로 phone도 함께 반환
        return new VerifyResult(true, token, pv.getPhone());
    }

    // 응답 DTO (internal)
    public record StartResult(String requestId, int resendAvailableInSec) {}
    public record VerifyResult(boolean verified, String kycToken, String phone) {}
}
