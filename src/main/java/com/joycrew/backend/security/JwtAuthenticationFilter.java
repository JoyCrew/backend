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
  // AntPathMatcher를 사용하여 URL 패턴을 비교합니다. (e.g., /api/docs/**)
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  // 1. 여기에 JWT 토큰 검사를 건너뛸 경로 목록을 정의합니다.
  private static final List<String> EXCLUDE_URLS = Arrays.asList(
          "/",
          "/h2-console/**",
          "/api/auth/login",
          "/api/auth/password-reset/**",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/api/products/**",
          "/api/crawl/**",
          // 최초 관리자 등록을 위해 이 경로를 필터 예외 목록에 추가합니다.
          "/api/admin/employees"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {

    // 2. 현재 요청 경로가 EXCLUDE_URLS 목록에 포함되는지 확인합니다.
    String path = request.getServletPath();
    boolean isExcluded = EXCLUDE_URLS.stream()
            .anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, path));

    // 3. 예외 목록에 포함된 경로라면, 필터 로직을 실행하지 않고 즉시 다음 필터로 넘깁니다.
    if (isExcluded) {
      log.info("JWT Filter bypassed for path: {}", path);
      filterChain.doFilter(request, response);
      return;
    }

    // --- 아래는 기존 필터 로직과 동일합니다 ---

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
