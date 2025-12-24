package com.joycrew.backend.service;

import com.joycrew.backend.dto.toss.TossIssueBillingKeyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossBillingKeyService {

    @Value("${toss.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ISSUE_URL =
            "https://api.tosspayments.com/v1/billing/authorizations/issue"; // 공식 엔드포인트 :contentReference[oaicite:3]{index=3}

    public String issueBillingKey(String authKey, String customerKey) {
        String auth = Base64.getEncoder().encodeToString((secretKey + ":")
                .getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("authKey", authKey);
        body.put("customerKey", customerKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TossIssueBillingKeyResponse> response =
                restTemplate.postForEntity(ISSUE_URL, entity, TossIssueBillingKeyResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Issue billingKey failed");
        }

        String billingKey = response.getBody().billingKey();
        if (billingKey == null || billingKey.isBlank()) {
            throw new IllegalStateException("billingKey is empty");
        }

        return billingKey;
    }
}
