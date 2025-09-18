package com.joycrew.backend.config;

import com.joycrew.backend.kyc.NicePassProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(NicePassProperties.class)
public class NicePassConfig {

    @Bean
    public RestTemplate niceRestTemplate(NicePassProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeoutMs());
        factory.setReadTimeout(props.getTimeoutMs());
        return new RestTemplate(factory);
    }
}
