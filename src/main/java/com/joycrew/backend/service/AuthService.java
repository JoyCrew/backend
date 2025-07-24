package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Employee employee = employeeRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

            String accessToken = jwtUtil.generateToken(employee.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .message("로그인 성공")
                    .userId(employee.getEmployeeId())
                    .name(employee.getEmployeeName())
                    .email(employee.getEmail())
                    .role(employee.getRole())
                    .build();
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.warn("Login failed for email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during login for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("로그인 중 서버 오류가 발생했습니다.");
        }
    }

    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout request received without a valid Bearer token.");
            return;
        }

        jwt = authHeader.substring(7);
        log.info("Logout request received for a token. In a real application, this token should be blacklisted.");
    }
}
