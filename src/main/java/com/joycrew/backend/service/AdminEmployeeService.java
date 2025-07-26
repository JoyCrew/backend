package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public Employee registerEmployee(EmployeeRegistrationRequest request) {
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사 ID입니다."));
        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서 ID입니다."));
        }
        Employee newEmployee = Employee.builder()
                .employeeName(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.initialPassword()))
                .company(company)
                .department(department)
                .position(request.position())
                .role(request.role())
                .status("ACTIVE")
                .build();
        Employee savedEmployee = employeeRepository.save(newEmployee);
        walletRepository.save(new Wallet(savedEmployee));
        return savedEmployee;
    }
}