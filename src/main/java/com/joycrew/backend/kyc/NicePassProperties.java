package com.joycrew.backend.kyc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "nice.pass")
public class NicePassProperties {
    private String baseUrl;
    private String standardWindowUrl;
    private String tokenVersionId;
    private String clientId;
    private String clientSecret;
    private String productId;
    private String returnUrl;
    private int timeoutMs = 7000;
    private int reqTtlSeconds = 600;
}
