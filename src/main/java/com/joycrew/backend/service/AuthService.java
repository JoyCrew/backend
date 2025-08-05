package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final long PASSWORD_RESET_EXPIRATION_MS = 15 * 60 * 1000;

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Employee employee = userPrincipal.getEmployee();

            Integer totalPoint = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                    .map(Wallet::getBalance)
                    .orElse(0);

            employee.updateLastLogin();

            String accessToken = jwtUtil.generateToken(employee.getEmail());

            return new LoginResponse(
                    accessToken,
                    "로그인 성공",
                    employee.getEmployeeId(),
                    employee.getEmployeeName(),
                    employee.getEmail(),
                    employee.getRole(),
                    totalPoint,
                    employee.getProfileImageUrl()
            );

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.warn("Login failed for email {}: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            log.info("Logout request received. Token blacklisting can be implemented here.");
        }
    }

    @Transactional(readOnly = true)
    public void requestPasswordReset(String email) {
        employeeRepository.findByEmail(email).ifPresent(employee -> {
            String token = jwtUtil.generateToken(email, PASSWORD_RESET_EXPIRATION_MS);
            emailService.sendPasswordResetEmail(email, token);
            log.info("비밀번호 재설정 요청 처리: {}", email);
        });
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String email;
        try {
            email = jwtUtil.getEmailFromToken(token);
        } catch (JwtException e) {
            throw new BadCredentialsException("유효하지 않거나 만료된 토큰입니다.", e);
        }

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        employee.changePassword(newPassword, passwordEncoder);
        log.info("비밀번호 재설정 완료: {}", email);
    }
}