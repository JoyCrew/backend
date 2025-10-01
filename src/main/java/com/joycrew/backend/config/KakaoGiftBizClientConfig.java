package com.joycrew.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class KakaoGiftBizClientConfig {

    @Bean
    public RestTemplate kakaoGiftBizRestTemplate(
            @Value("${kakao.giftbiz.timeout-ms:5000}") long timeoutMs) {

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeoutMs);
        factory.setReadTimeout((int) timeoutMs);

        return new RestTemplateBuilder()
                .requestFactory(() -> new BufferingClientHttpRequestFactory(factory))
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }
}
