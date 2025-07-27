package com.joycrew.backend.service;

import com.joycrew.backend.dto.CreateEmployeeRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAdminService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public Employee createEmployee(CreateEmployeeRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("회사 ID가 유효하지 않습니다."));

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException("부서 ID가 유효하지 않습니다."));
        }

        Employee employee = Employee.builder()
                .company(company)
                .department(department)
                .employeeName(request.employeeName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.rawPassword()))
                .position(request.position())
                .profileImageUrl(request.profileImageUrl())
                .personalEmail(request.personalEmail())
                .phoneNumber(request.phoneNumber())
                .shippingAddress(request.shippingAddress())
                .emailNotificationEnabled(request.emailNotificationEnabled())
                .appNotificationEnabled(request.appNotificationEnabled())
                .language(request.language())
                .timezone(request.timezone())
                .role(request.role() != null ? request.role() : UserRole.EMPLOYEE)
                .status("ACTIVE")
                .build();

        return employeeRepository.save(employee);
    }
}
