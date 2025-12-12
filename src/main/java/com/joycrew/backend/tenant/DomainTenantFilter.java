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

        String host = extractHost(http);         // X-Forwarded-Host 우선
        String normalized = normalizeHost(host); // 포트 제거, 소문자 변환

        // 공통 도메인(메인, API, 로컬)은 테넌트 설정 없이 통과
        // 이유: 로그인 등에서 전체 회사를 조회해야 하는 경우가 있기 때문
        if (isCommonDomain(normalized)) {
            chain.doFilter(req, res);
            return;
        }

        Long companyId = resolveCompanyId(normalized)
                .orElseGet(this::fallbackCompanyId); // 없으면 기본값(개발/로컬용)

        try {
            TenantContext.set(companyId);
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    // 공통 도메인인지 확인하는 메서드
    private boolean isCommonDomain(String host) {
        if (host == null) return false;
        return host.equals("joycrew.co.kr") ||
                host.equals("api.joycrew.co.kr") ||
                host.equals("www.joycrew.co.kr") ||
                host.equals("localhost") ||
                host.equals("127.0.0.1");
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
        int idx = host.indexOf(':'); // :443 등 제거
        String h = (idx > -1) ? host.substring(0, idx) : host;
        return h.toLowerCase();
    }
}