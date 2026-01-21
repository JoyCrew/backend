package com.joycrew.backend.kakao;

import com.joycrew.backend.dto.kakao.KakaoTemplateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.net.URI;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class KakaoGiftBizClient {

    private final RestTemplate rt;

    @Value("${kakao.giftbiz.base-url}")
    private String baseUrl;

    @Value("${kakao.giftbiz.api-key}")
    private String apiKey;

    public KakaoGiftBizClient(@Qualifier("kakaoGiftBizRestTemplate") RestTemplate rt) {
        this.rt = rt;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        // Kakao Developers 인증: "KakaoAK <키>"
        h.set("Authorization", "KakaoAK " + apiKey);
        h.set("User-Agent", "JoyCrewBackend/1.0");
        return h;
    }

    /**
     * 선물 발송 요청 (v1/template/order)
     * 성공 시 바디 파싱을 하지 않고 String으로만 로깅한다. (응답 스펙 변화/빈 바디로 인한 파싱 실패 방지)
     * 4xx는 BAD_REQUEST로, I/O는 BAD_GATEWAY로 매핑하여 상위로 던진다.
     */
    public String sendTemplateOrder(KakaoTemplateOrderRequest req) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/v1/template/order")
                .build(true)
                .toUri();

        try {
            log.debug("[KAKAO] ORDER -> {} body={}", uri, req);
            ResponseEntity<String> res = rt.exchange(
                    uri, HttpMethod.POST, new HttpEntity<>(req, authHeaders()), String.class);

            log.info("[KAKAO] ORDER <- status={} hasBody={}", res.getStatusCodeValue(), res.hasBody());
            if (res.hasBody()) {
                log.debug("[KAKAO] ORDER BODY: {}", res.getBody());
            }
            return res.getBody();

        } catch (HttpClientErrorException ex) {
            // Kakao 4xx 그대로 400으로 매핑 + 본문 노출
            String body = ex.getResponseBodyAsString();
            log.warn("[KAKAO] 4xx order failed: status={} body={}", ex.getStatusCode().value(), body);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "KAKAO_ERROR: " + body, ex);

        } catch (ResourceAccessException ex) {
            // 네트워크/DNS/타임아웃 등 I/O 오류 → 502로 매핑
            log.error("[KAKAO] I/O error: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KAKAO_IO_ERROR: " + ex.getMessage(), ex);
        }
    }
}
