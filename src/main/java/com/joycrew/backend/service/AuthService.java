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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.password-reset-expiration-ms}")
    private long passwordResetExpirationMs;

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
                    "Login successful",
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
            log.info("Logout request received. Token blacklisting can be implemented here if needed.");
        }
    }

    @Transactional(readOnly = true)
    public void requestPasswordReset(String email) {
        employeeRepository.findByEmail(email).ifPresent(employee -> {
            String token = jwtUtil.generateToken(email, passwordResetExpirationMs);
            emailService.sendPasswordResetEmail(email, token);
            log.info("Password reset requested for email: {}", email);
        });
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String email;
        try {
            email = jwtUtil.getEmailFromToken(token);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired token.", e);
        }

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        employee.changePassword(newPassword, passwordEncoder);
        log.info("Password has been reset for: {}", email);
    }
}