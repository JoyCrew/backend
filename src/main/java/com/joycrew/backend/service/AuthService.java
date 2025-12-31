package com.joycrew.backend.service;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.repository.CompanyDomainRepository;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.tenant.TenantContext; // Tenant 대신 Context 직접 참조
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

import java.util.Optional;

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

      Long userCompanyId = employee.getCompany().getCompanyId();

      String subdomain = companyDomainRepository
              .findFirstByCompanyCompanyIdAndPrimaryDomainTrueOrderByIdDesc(userCompanyId)
              .map(cd -> cd.getDomain().toLowerCase())
              .orElse(null);

      boolean isAdmin = employee.getRole() == AdminLevel.HR_ADMIN || employee.getRole() == AdminLevel.SUPER_ADMIN;
      boolean billingRequired = isAdmin && !employee.getCompany().isBillingReady();

      return new LoginResponse(
              accessToken,
              "Login successful",
              employee.getEmployeeId(),
              employee.getEmployeeName(),
              employee.getEmail(),
              employee.getRole(),
              totalPoint,
              employee.getProfileImageUrl(),
              subdomain,
              billingRequired
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
   * 비밀번호 재설정 요청:
   * 1. 테넌트 도메인이면 해당 테넌트에서 검색
   * 2. 공통 도메인(api 등)이면 전체에서 이메일로 검색
   */
  @Transactional(readOnly = true)
  public void requestPasswordReset(String email) {
    Long tenantId = TenantContext.get();
    Optional<Employee> employeeOpt;

    if (tenantId != null) {
      employeeOpt = employeeRepository.findByCompanyCompanyIdAndEmail(tenantId, email);
    } else {
      employeeOpt = employeeRepository.findByEmail(email);
    }

    employeeOpt.ifPresent(emp -> {
      String token = jwtUtil.generateToken(email, passwordResetExpirationMs);

      log.info("==== DEBUG RESET TOKEN FOR {} : {} ====", email, token);

      emailService.sendPasswordResetEmail(email, token);
      log.info("Password reset requested for email: {} (Search mode: {})",
              email, (tenantId != null ? "Tenant " + tenantId : "Global"));
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

    Long tenantId = TenantContext.get();
    Employee employee;

    if (tenantId != null) {
      employee = employeeRepository.findByCompanyCompanyIdAndEmail(tenantId, email)
              .orElseThrow(() -> new UserNotFoundException("User not found in this company."));
    } else {
      employee = employeeRepository.findByEmail(email)
              .orElseThrow(() -> new UserNotFoundException("User not found globally."));
    }

    employee.changePassword(newPassword, passwordEncoder);
    log.info("Password has been reset for: {} (Search mode: {})",
            email, (tenantId != null ? "Tenant " + tenantId : "Global"));
  }
}