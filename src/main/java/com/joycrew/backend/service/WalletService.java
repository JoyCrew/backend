package com.joycrew.backend.service;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import com.joycrew.backend.tenant.Tenant;
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

    @Transactional
    public PointBalanceResponse getPointBalance(String userEmail) {
        Long tenant = Tenant.id();

        Employee employee = employeeRepository
                .findByCompanyCompanyIdAndEmail(tenant, userEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));

        Wallet wallet = walletRepository
                .findByEmployeeCompanyCompanyIdAndEmployeeEmployeeId(tenant, employee.getEmployeeId())
                .orElseGet(() -> walletRepository.save(new Wallet(employee)));

        return employeeMapper.toPointBalanceResponse(wallet);
    }
}
