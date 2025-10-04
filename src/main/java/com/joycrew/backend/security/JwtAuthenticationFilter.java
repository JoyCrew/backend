package com.joycrew.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    // JWT 토큰 검사를 건너뛸 경로 목록 (SecurityConfig와 일치하도록 수정)
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/",
            "/error",
            "/actuator/health",
            "/h2-console/**",
            "/api/auth/**",                  // 로그인, 비밀번호 재설정 등 모든 인증 관련 경로
            "/api/kyc/phone/**",             // ### KYC 관련 경로 추가 (문제의 직접적인 원인) ###
            "/accounts/emails/by-phone",     // ### 이메일 조회 경로 추가 (문제의 직접적인 원인) ###
            "/api/catalog/**",               // 상품 목록 조회 경로 추가
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
            // "/api/admin/employees" // 보안상 이 경로는 필터 예외에서 제거하는 것이 올바릅니다.
    );

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

        boolean isExcluded = EXCLUDE_URLS.stream()
                .anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, path));

        if (isExcluded) {
            log.info("JWT Filter bypassed for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("===== JWT Filter Executed for path: {} =====", path);

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or invalid for protected path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = null;
        try {
            email = jwtUtil.getEmailFromToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User '{}' authenticated successfully.", email);
        }

        filterChain.doFilter(request, response);
    }
}