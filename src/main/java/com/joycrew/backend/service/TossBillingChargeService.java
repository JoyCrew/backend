package com.joycrew.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossBillingChargeService {

    @Value("${toss.secret-key:}")
    private String secretKey;

    @Value("${subscription.monthly-price}")
    private long monthlyPrice;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BILLING_URL =
            "https://api.tosspayments.com/v1/billing/{billingKey}";

    public record TossChargeResult(
            boolean success,
            String paymentKey,
            LocalDateTime approvedAt,
            String failCode,
            String failMessage,
            String rawResponse
    ) {}

    public TossChargeResult charge(Company company, String orderId) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("toss.secret-key is missing");
        }
        if (company.getTossBillingKey() == null || company.getTossBillingKey().isBlank()) {
            throw new IllegalStateException("billingKey missing");
        }
        if (company.getTossCustomerKey() == null || company.getTossCustomerKey().isBlank()) {
            throw new IllegalStateException("customerKey missing");
        }

        String basic = Base64.getEncoder().encodeToString((secretKey + ":")
                .getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("customerKey", company.getTossCustomerKey());
        body.put("amount", monthlyPrice);
        body.put("orderId", orderId);
        body.put("orderName", "JoyCrew 월 구독");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> res =
                    restTemplate.postForEntity(BILLING_URL, entity, String.class, company.getTossBillingKey());

            String raw = res.getBody();
            String paymentKey = null;
            LocalDateTime approvedAt = null;

            if (raw != null) {
                JsonNode node = objectMapper.readTree(raw);
                paymentKey = node.path("paymentKey").asText(null);
                // approvedAt 포맷은 토스 응답에 맞게 파싱 필요할 수 있음 (없으면 null 허용)
            }

            return new TossChargeResult(true, paymentKey, approvedAt, null, null, raw);

        } catch (HttpStatusCodeException e) {
            String raw = e.getResponseBodyAsString();
            log.error("[TOSS][CHARGE] status={}, body={}", e.getStatusCode(), raw);

            return new TossChargeResult(
                    false,
                    null,
                    null,
                    "HTTP_" + e.getStatusCode().value(),
                    e.getStatusText(),
                    raw
            );
        } catch (Exception e) {
            log.error("[TOSS][CHARGE] unexpected error", e);
            return new TossChargeResult(false, null, null, "EXCEPTION", e.getMessage(), null);
        }
    }
}
