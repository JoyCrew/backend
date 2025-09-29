package com.joycrew.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class KycTokenService {

    @Value("${app.kyc.token-secret}") private String secret;
    @Value("${app.kyc.token-ttl-minutes:10}") private int ttlMin;

    public String create(String phone) {
        long now = Instant.now().getEpochSecond();
        long exp = now + ttlMin * 60L;
        String payload = phone + "|" + now + "|" + exp;
        String sig = hmac(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "|" + sig).getBytes(StandardCharsets.UTF_8));
    }

    public String validateAndExtractPhone(String token) {
        var raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] p = raw.split("\\|");
        if (p.length != 4) throw new IllegalArgumentException("bad token");
        String phone = p[0];
        long exp = Long.parseLong(p[2]);
        String sig = p[3];
        String expected = hmac(p[0] + "|" + p[1] + "|" + p[2]);
        if (!expected.equals(sig)) throw new IllegalArgumentException("invalid signature");
        if (Instant.now().getEpochSecond() > exp) throw new IllegalArgumentException("expired");
        return phone;
    }

    private String hmac(String msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    mac.doFinal(msg.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}