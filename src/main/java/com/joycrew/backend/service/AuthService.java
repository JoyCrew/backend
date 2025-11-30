package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.repository.CompanyDomainRepository;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.tenant.Tenant;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.password-reset-expiration-ms}")
    private long passwordResetExpirationMs;

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyDomainRepository companyDomainRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * 로그인: 인증 성공 시 JWT와 사용자 정보 + subdomain(예: alko.joycrew.co.kr)을 반환
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.email());

        try {
            // 1. 인증 진행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Employee employee = principal.getEmployee();

            // 2. 지갑 잔액 조회
            Integer totalPoint = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                    .map(Wallet::getBalance)
                    .orElse(0);

            // 3. 마지막 로그인 시간 업데이트
            employee.updateLastLogin();

            // 4. 토큰 생성
            String accessToken = jwtUtil.generateToken(employee.getEmail());

            // 🚨 [핵심 수정]
            // 기존: Tenant.id() -> 현재 접속한 URL(joycrew.co.kr)을 기준으로 찾음 (실패 원인)
            // 수정: employee.getCompany().getCompanyId() -> '로그인한 유저의 소속 회사'를 기준으로 찾음 (정답)
            Long userCompanyId = employee.getCompany().getCompanyId();

            String subdomain = companyDomainRepository
                    .findFirstByCompanyCompanyIdAndPrimaryDomainTrueOrderByIdDesc(userCompanyId)
                    .map(cd -> cd.getDomain().toLowerCase())
                    .orElse(null);

            return new LoginResponse(
                    accessToken,
                    "Login successful",
                    employee.getEmployeeId(),
                    employee.getEmployeeName(),
                    employee.getEmail(),
                    employee.getRole(),
                    totalPoint,
                    employee.getProfileImageUrl(),
                    subdomain // 이제 BDL 유저는 'bdl.joycrew.co.kr'을 반환받음
            );

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.warn("Login failed for email {}: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    /**
     * 로그아웃
     */
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            log.info("Logout requested (token: {}). Add blacklist handling if needed.", jwt);
        }
    }

    /**
     * 비밀번호 재설정 요청
     */
    @Transactional(readOnly = true)
    public void requestPasswordReset(String email) {
        // 비밀번호 변경은 현재 접속한 도메인 컨텍스트를 유지하는 것이 안전할 수 있음
        Long tenant = Tenant.id();
        employeeRepository.findByCompanyCompanyIdAndEmail(tenant, email).ifPresent(emp -> {
            String token = jwtUtil.generateToken(email, passwordResetExpirationMs);
            emailService.sendPasswordResetEmail(email, token);
            log.info("Password reset requested for email: {} (tenant={})", email, tenant);
        });
    }

    /**
     * 비밀번호 재설정 확정
     */
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String email;
        try {
            email = jwtUtil.getEmailFromToken(token);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired token.", e);
        }

        Long tenant = Tenant.id();
        Employee employee = employeeRepository
                .findByCompanyCompanyIdAndEmail(tenant, email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        employee.changePassword(newPassword, passwordEncoder);
        log.info("Password has been reset for: {} (tenant={})", email, tenant);
    }
}