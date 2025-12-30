package com.joycrew.backend.security;

import com.joycrew.backend.tenant.TenantContext;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // JWT 토큰 검사를 건너뛸 경로 목록 (SecurityConfig와 일치하도록 유지)
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/",
            "/error",
            "/actuator/health",
            "/actuator/prometheus",
            "/h2-console/**",
            "/api/auth/**",
            "/api/kyc/phone/**",
            "/accounts/emails/by-phone",
            "/api/catalog/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    );

    /**
     * Authorization 헤더 또는 JC_AUTH 쿠키에서 토큰을 추출한다.
     */
    private String resolveToken(HttpServletRequest request) {
        // 1순위: Authorization 헤더
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2순위: 쿠키(JC_AUTH)에서 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JC_AUTH".equals(c.getName())) {
                    String value = c.getValue();
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }

        return null;
    }

    private boolean isExcluded(String path) {
        return EXCLUDE_URLS.stream()
                .anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // CORS Preflight 요청(OPTIONS)은 항상 통과
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 화이트리스트 경로는 JWT 검사 스킵
        if (isExcluded(path)) {
            log.debug("JWT Filter bypassed for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("===== JWT Filter Executed for path: {} =====", path);

        // 헤더 또는 쿠키에서 토큰 추출
        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            log.warn("No JWT token found for protected path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String email = null;
        boolean tenantSetByJwt = false;

        try {
            try {
                email = jwtUtil.getEmailFromToken(token);
            } catch (ExpiredJwtException e) {
                log.warn("JWT token has expired: {}", e.getMessage());
            } catch (JwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("User '{}' authenticated successfully.", email);

                // 🔁 도메인 필터가 테넌트를 못 설정한 경우, JWT에서 유저 회사 기준으로 fallback 설정
                if (TenantContext.get() == null && userDetails instanceof UserPrincipal principal) {
                    Long userCompanyId = principal.getEmployee().getCompany().getCompanyId();
                    TenantContext.set(userCompanyId);
                    tenantSetByJwt = true;
                    log.debug("Tenant fallback: Set to Company ID {} from JWT UserPrincipal", userCompanyId);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // 이 필터에서 테넌트 컨텍스트를 세팅한 경우만 책임지고 정리
            if (tenantSetByJwt) {
                TenantContext.clear();
            }
        }
    }
}
