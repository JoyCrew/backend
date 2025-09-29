package com.joycrew.backend.config;

import com.joycrew.backend.service.sms.SmsSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SmsConfig {

    @Value("${app.sms.provider:console}")
    String provider;

    @Bean
    @Primary
    public SmsSender smsSender(@Qualifier("consoleSmsSender") SmsSender console,
                               @Qualifier("solapiSmsSender") SmsSender solapi) {
        return "solapi".equalsIgnoreCase(provider) ? solapi : console;
    }
}