package com.joycrew.backend.service;

import com.joycrew.backend.dto.toss.TossIssueBillingKeyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossBillingKeyService {

    @Value("${toss.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate;

    private static final String ISSUE_URL =
            "https://api.tosspayments.com/v1/billing/authorizations/issue";

    public String issueBillingKey(String authKey, String customerKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("toss.secret-key is missing");
        }
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalArgumentException("authKey is blank");
        }
        if (customerKey == null || customerKey.isBlank()) {
            throw new IllegalArgumentException("customerKey is blank");
        }

        String basic = Base64.getEncoder().encodeToString((secretKey + ":")
                .getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("authKey", authKey);
        body.put("customerKey", customerKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TossIssueBillingKeyResponse> response =
                    restTemplate.postForEntity(ISSUE_URL, entity, TossIssueBillingKeyResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Issue billingKey failed (empty response)");
            }

            String billingKey = response.getBody().billingKey();
            if (billingKey == null || billingKey.isBlank()) {
                throw new IllegalStateException("billingKey is empty");
            }

            return billingKey;

        } catch (HttpStatusCodeException e) {
            log.error("[TOSS][ISSUE] status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Toss issue billingKey failed: " + e.getStatusCode(), e);
        }
    }
}
