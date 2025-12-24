package com.joycrew.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${subscription.monthly-price}")
    private long monthlyPrice;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BILLING_URL =
            "https://api.tosspayments.com/v1/billing/{billingKey}";

    public TossChargeResult charge(Company company, String orderId) {
        String billingKey = company.getTossBillingKey();
        String customerKey = company.getTossCustomerKey();

        if (billingKey == null || customerKey == null) {
            throw new IllegalStateException("Company has no billingKey/customerKey");
        }

        String auth = Base64.getEncoder().encodeToString((secretKey + ":")
                .getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("customerKey", customerKey);
        body.put("orderId", orderId);
        body.put("amount", monthlyPrice);
        body.put("orderName", "JoyCrew 월 구독");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(BILLING_URL, entity, String.class, billingKey);

        String raw = response.getBody();

        if (!response.getStatusCode().is2xxSuccessful()) {
            return TossChargeResult.failed(
                    "HTTP_" + response.getStatusCode().value(),
                    "Billing failed",
                    raw
            );
        }

        String paymentKey = null;
        LocalDateTime approvedAt = null;

        try {
            if (raw != null) {
                JsonNode node = objectMapper.readTree(raw);
                if (node.hasNonNull("paymentKey")) paymentKey = node.get("paymentKey").asText();
                if (node.hasNonNull("approvedAt")) {
                    approvedAt = LocalDateTime.parse(node.get("approvedAt").asText().replace("Z", ""));
                }
            }
        } catch (Exception e) {
            log.warn("[TOSS-PARSE-WARN] cannot parse billing response. raw={}", raw);
        }

        return TossChargeResult.success(paymentKey, approvedAt, raw);
    }

    public record TossChargeResult(
            boolean success,
            String paymentKey,
            LocalDateTime approvedAt,
            String failCode,
            String failMessage,
            String rawResponse
    ) {
        public static TossChargeResult success(String paymentKey, LocalDateTime approvedAt, String raw) {
            return new TossChargeResult(true, paymentKey, approvedAt, null, null, raw);
        }

        public static TossChargeResult failed(String failCode, String failMessage, String raw) {
            return new TossChargeResult(false, null, null, failCode, failMessage, raw);
        }
    }
}
