package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminPointBudgetResponse;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

  private final EmployeeRepository employeeRepository;
  private final WalletRepository walletRepository;

  /**
   * Fetches both the company's total point budget and the admin's personal wallet balance.
   * @param adminEmail The email of the currently logged-in administrator.
   * @return A DTO containing both company and personal point balances.
   */
  public AdminPointBudgetResponse getAdminAndCompanyBalance(String adminEmail) {
    // 1. Fetch the admin employee and their associated company
    Employee admin = employeeRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new UserNotFoundException("Admin user not found."));

    Company company = admin.getCompany();
    if (company == null) {
      throw new IllegalStateException("Admin is not associated with any company.");
    }

    // 2. Fetch the admin's personal wallet
    Wallet adminWallet = walletRepository.findByEmployee_EmployeeId(admin.getEmployeeId())
        .orElse(new Wallet(admin)); // If no wallet, create a new one with 0 points

    // 3. Create and return the combined response DTO
    return new AdminPointBudgetResponse(
            (double)company.getTotalCompanyBalance(),
        adminWallet.getBalance(),
        adminWallet.getGiftablePoint()
    );
  }
}
