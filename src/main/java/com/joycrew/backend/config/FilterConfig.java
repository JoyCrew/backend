// src/main/java/com/joycrew/backend/config/FilterConfig.java
package com.joycrew.backend.config;

import com.joycrew.backend.tenant.DomainTenantFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<DomainTenantFilter> tenantFilterRegistration(DomainTenantFilter filter) {
        FilterRegistrationBean<DomainTenantFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);
        reg.setOrder(1);           // SecurityFilterChain보다 앞서도록 1 등 낮은 순서
        reg.addUrlPatterns("/*");  // 전 요청 적용
        return reg;
    }
}
