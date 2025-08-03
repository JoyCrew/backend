package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Employee employee = userPrincipal.getEmployee();

            // 지갑 정보를 조회하여 보유 포인트를 가져옵니다. 지갑이 없으면 0을 반환합니다.
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
            // TODO: Redis 등을 이용한 토큰 블랙리스트 처리 로직 추가
        }
    }
}