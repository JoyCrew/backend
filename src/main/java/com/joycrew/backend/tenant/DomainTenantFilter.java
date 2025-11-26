// src/main/java/com/joycrew/backend/tenant/DomainTenantFilter.java
package com.joycrew.backend.tenant;

import com.joycrew.backend.repository.CompanyDomainRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class DomainTenantFilter implements Filter {

    private final CompanyDomainRepository domainRepository;

    public DomainTenantFilter(CompanyDomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;

        String host = extractHost(http);               // X-Forwarded-Host 우선
        String normalized = normalizeHost(host);       // 포트 제거, 소문자 변환

        Long companyId = resolveCompanyId(normalized)
                .orElseGet(this::fallbackCompanyId);       // 없으면 기본값(개발/로컬용)

        try {
            TenantContext.set(companyId);
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    private Optional<Long> resolveCompanyId(String host) {
        if (host == null || host.isBlank()) return Optional.empty();
        return domainRepository.findCompanyIdByDomain(host);
    }

    private Long fallbackCompanyId() {
        // 운영에선 404(UNKNOWN DOMAIN)로 처리하고 싶다면 예외를 던지도록 바꾸세요.
        // throw new ServletException("Unknown domain");
        return 1L; // 개발/로컬 환경 기본 테넌트
    }

    private String extractHost(HttpServletRequest http) {
        String fwd = http.getHeader("X-Forwarded-Host");
        if (fwd != null && !fwd.isBlank()) return fwd.split(",")[0].trim();
        return http.getHeader("Host");
    }

    private String normalizeHost(String host) {
        if (host == null) return null;
        int idx = host.indexOf(':');                   // :443 등 제거
        String h = (idx > -1) ? host.substring(0, idx) : host;
        return h.toLowerCase();
    }
}
