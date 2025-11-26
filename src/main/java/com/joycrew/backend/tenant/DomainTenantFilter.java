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

        String host = extractHost(http);
        String normalized = normalizeHost(host);

        // 🚨 [핵심 수정] 공통 도메인(메인, 로컬)은 테넌트 설정(필터) 없이 그냥 통과!
        // 이유: 로그인 시 전체 회사를 뒤져서 유저를 찾아야 하기 때문.
        if (isCommonDomain(normalized)) {
            chain.doFilter(req, res);
            return;
        }

        Long companyId = resolveCompanyId(normalized)
                .orElseGet(this::fallbackCompanyId);

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
                host.equals("www.joycrew.co.kr") ||
                host.equals("localhost") ||
                host.equals("127.0.0.1");
    }

    private Optional<Long> resolveCompanyId(String host) {
        if (host == null || host.isBlank()) return Optional.empty();
        return domainRepository.findCompanyIdByDomain(host);
    }

    private Long fallbackCompanyId() {
        return 1L; // 알 수 없는 서브도메인일 때만 1번으로 fallback
    }

    private String extractHost(HttpServletRequest http) {
        String fwd = http.getHeader("X-Forwarded-Host");
        if (fwd != null && !fwd.isBlank()) return fwd.split(",")[0].trim();
        return http.getHeader("Host");
    }

    private String normalizeHost(String host) {
        if (host == null) return null;
        int idx = host.indexOf(':');
        String h = (idx > -1) ? host.substring(0, idx) : host;
        return h.toLowerCase();
    }
}