package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminPointBudgetResponse;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

  private final EmployeeRepository employeeRepository;
  private final WalletRepository walletRepository;

  public AdminPointBudgetResponse getAdminAndCompanyBalance(String adminEmail) {

    Long companyId = Tenant.id();

    // tenant 범위에서 admin 조회
    Employee admin = employeeRepository.findByCompanyCompanyIdAndEmail(companyId, adminEmail)
            .orElseThrow(() -> new UserNotFoundException("Admin user not found."));

    Company company = admin.getCompany();
    if (company == null) {
      throw new IllegalStateException("Admin is not associated with any company.");
    }

    Wallet adminWallet = walletRepository.findByEmployee_EmployeeId(admin.getEmployeeId())
            .orElse(new Wallet(admin));

    // Double 그대로 반환
    return new AdminPointBudgetResponse(
            company.getTotalCompanyBalance(),
            adminWallet.getBalance(),
            adminWallet.getGiftablePoint()
    );
  }
}
