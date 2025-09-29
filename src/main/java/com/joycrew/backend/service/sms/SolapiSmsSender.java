package com.joycrew.backend.service.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component("solapiSmsSender")
@RequiredArgsConstructor
public class SolapiSmsSender implements SmsSender {

    private final SolapiProps props; // apiKey, apiSecret, baseUrl 바인딩되어 있다고 가정
    private final RestClient client = RestClient.create();

    // app.sms.from-number 에서 바인딩 받는 버전이라면 그대로 사용
    @org.springframework.beans.factory.annotation.Value("${app.sms.from-number:}")
    private String fromNumber;

    @PostConstruct
    void check() {
        log.info("[SMS CONFIG] solapi baseUrl={}, hasKey={}, hasSecret={}, from={}",
                props.getBaseUrl(), props.getApiKey()!=null, props.getApiSecret()!=null, fromNumber);
    }

    @Override
    public void send(String toPhone, String message) {
        // ---- 사전 검증 ----
        if (isBlank(fromNumber)) throw new IllegalStateException("app.sms.from-number is missing. Use digits only.");
        if (isBlank(props.getApiKey()) || isBlank(props.getApiSecret())) throw new IllegalStateException("solapi api-key/secret missing.");
        if (isBlank(props.getBaseUrl())) throw new IllegalStateException("solapi base-url missing (e.g., https://api.solapi.com).");

        // ---- 값 정리 ----
        String to = toPhone.replaceAll("\\D", "");
        String from = fromNumber.replaceAll("\\D", "");

        // ---- Authorization: HMAC-SHA256 ... 생성 ----
        String dateTime = Instant.now().toString();                // ISO8601 UTC (예: 2025-09-11T13:45:00Z)
        String salt = UUID.randomUUID().toString().replace("-", ""); // 12~64자 임의 문자열
        String signature = hmacSha256Hex(props.getApiSecret(), dateTime + salt);
        String authHeader = "HMAC-SHA256 apiKey=%s, date=%s, salt=%s, signature=%s"
                .formatted(props.getApiKey(), dateTime, salt, signature);

        // ---- Body: send-many/detail 스펙에 맞춤 ----
        Map<String, Object> msg = new HashMap<>();
        msg.put("to", to);
        msg.put("from", from);
        msg.put("text", message);

        Map<String, Object> body = new HashMap<>();
        body.put("messages", List.of(msg));

        String url = props.getBaseUrl() + "/messages/v4/send-many/detail";

        try {
            var res = client.post()
                    .uri(url)
                    .header("Authorization", authHeader) // << HMAC 인증
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            log.info("[SOLAPI] sent to={}, res={}", to, res);
        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.error("[SOLAPI] {} {} body={}", e.getRawStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("[SOLAPI] send failed", e);
            throw e;
        }
    }

    private static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create HMAC-SHA256 signature", e);
        }
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}