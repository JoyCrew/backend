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

    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/", "/error", "/actuator/health", "/h2-console/**",
            "/api/auth/**", "/api/kyc/phone/**", "/accounts/emails/by-phone",
            "/api/catalog/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    );

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("accessToken".equals(c.getName())) {
                    String value = c.getValue();
                    if (value != null && !value.isBlank()) return value;
                }
            }
        }
        return null;
    }

    private boolean isExcluded(String path) {
        return EXCLUDE_URLS.stream().anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (request.getMethod().equalsIgnoreCase("OPTIONS") || isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            // log.warn("No JWT token found for protected path: {}", path);
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
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 🚨 [수정됨] 메서드 이름 변경: getCurrentTenant -> get, setCurrentTenant -> set
                if (TenantContext.get() == null && userDetails instanceof UserPrincipal) {
                    UserPrincipal principal = (UserPrincipal) userDetails;
                    Long userCompanyId = principal.getEmployee().getCompany().getCompanyId();

                    TenantContext.set(userCompanyId); // 👈 여기 수정됨
                    tenantSetByJwt = true;
                    log.debug("Tenant fallback: Set to Company ID {} from JWT UserPrincipal", userCompanyId);
                }
            }
            filterChain.doFilter(request, response);

        } finally {
            if (tenantSetByJwt) {
                TenantContext.clear();
            }
        }
    }
}