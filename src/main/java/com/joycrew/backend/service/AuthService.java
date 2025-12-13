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

            // 5. 유저의 회사 ID 기반 서브도메인 찾기
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
                    subdomain
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
        // ✅ [수정 1] Tenant.id() 제거. 로그인 전이므로 Context가 없음.
        // 대신 이메일로 전역 검색 (findByEmail 사용)
        employeeRepository.findByEmail(email).ifPresent(emp -> {
            Long tenantId = emp.getCompany().getCompanyId(); // 로깅용으로 추출

            String token = jwtUtil.generateToken(email, passwordResetExpirationMs);
            emailService.sendPasswordResetEmail(email, token);

            log.info("Password reset requested for email: {} (companyId={})", email, tenantId);
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

        // ✅ [수정 2] 여기도 Tenant.id() 제거.
        // 토큰에 있는 이메일로 유저를 찾아서 비밀번호 변경
        Employee employee = employeeRepository
                .findByEmail(email) // findByCompanyCompanyIdAndEmail 대신 findByEmail 사용
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        employee.changePassword(newPassword, passwordEncoder);
        log.info("Password has been reset for: {} (companyId={})", email, employee.getCompany().getCompanyId());
    }
}