package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {
  private final WalletRepository walletRepository;
  private final EmployeeRepository employeeRepository;
  private final EmployeeMapper employeeMapper;

  public PointBalanceResponse getPointBalance(String userEmail) {
    Employee employee = employeeRepository.findByEmail(userEmail)
        .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));

    Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
        .orElse(new Wallet(employee));

    return employeeMapper.toPointBalanceResponse(wallet);
  }
}
