package com.joycrew.backend.kyc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NicePassClient {

    private final RestTemplate restTemplate;
    private final NicePassProperties props;

    // 1) 기관 액세스 토큰 발급 (client_credentials)
    public String getAccessToken() {
        String basic = props.getClientId() + ":" + props.getClientSecret();
        String auth = "Basic " + Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", "default");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", auth);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        ResponseEntity<Map> res = restTemplate.postForEntity(
                props.getBaseUrl() + "/digital/niceid/oauth/oauth/token",
                entity, Map.class);

        Map body = res.getBody();
        if (body == null || body.get("access_token") == null) {
            throw new IllegalStateException("Failed to get NICE access token");
        }
        return String.valueOf(body.get("access_token"));
    }

    // 2) 암호화 토큰(crypto token) 요청 (상품/버전에 따라 path/필드 차이 가능)
    public Map<String,Object> getCryptoToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String,Object> payload = Map.of(
                "productId", props.getProductId(),
                "tokenVersionId", props.getTokenVersionId()
        );

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> res = restTemplate.postForEntity(
                props.getBaseUrl() + "/digital/niceid/api/v1.0/common/crypto/token",
                entity, Map.class);

        Map body = res.getBody();
        if (body == null) {
            throw new IllegalStateException("Failed to get NICE crypto token");
        }
        //noinspection unchecked
        return (Map<String, Object>) body;
    }
}
