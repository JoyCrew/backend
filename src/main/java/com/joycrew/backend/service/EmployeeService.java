package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    @Transactional
    public void registerEmployee(String email, String rawPassword, String name, Company company) {
        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        Employee newEmployee = Employee.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .employeeName(name)
                .status("ACTIVE")
                .role(UserRole.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .company(company)
                .build();

        Employee savedEmployee = employeeRepository.save(newEmployee);

        Wallet newWallet = Wallet.builder()
                .employee(savedEmployee)
                .balance(0)
                .giftablePoint(0)
                .build();
        walletRepository.save(newWallet);

        savedEmployee.setWallet(newWallet);
        employeeRepository.save(savedEmployee);
    }
}