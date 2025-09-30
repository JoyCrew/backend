package com.joycrew.backend.service.sms;

import lombok.Getter; import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties("solapi")
public class SolapiProps {
    private String apiKey;
    private String apiSecret;
    private String baseUrl;
    private String fromNumber; // yml에선 app.sms.from-number, 주입은 아래 Config에서
}