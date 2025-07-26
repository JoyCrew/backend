package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
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
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * [HR 관리자 기능] 신규 직원을 등록합니다.
     * 초기 비밀번호가 설정되며, 첫 로그인 시 변경해야 합니다.
     *
     * @param request 신규 직원 정보 DTO
     * @return 생성된 Employee 엔티티
     */
    public Employee registerEmployee(EmployeeRegistrationRequest request) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사 ID입니다."));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서 ID입니다."));
        }

        Employee newEmployee = Employee.builder()
                .employeeName(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getInitialPassword()))
                .company(company)
                .department(department)
                .position(request.getPosition())
                .role(request.getRole())
                .status("ACTIVE")
                .build();

        Employee savedEmployee = employeeRepository.save(newEmployee);

        Wallet newWallet = Wallet.builder()
                .employee(savedEmployee)
                .balance(0)
                .giftablePoint(0)
                .build();
        walletRepository.save(newWallet);

        return savedEmployee;
    }

    /**
     * [HR 관리자 기능] 직원의 기본 정보를 수정합니다.
     *
     * @param employeeId 수정 대상 직원 ID
     * @param request    수정할 정보 DTO
     * @return 업데이트된 Employee 엔티티
     */
    public Employee updateEmployeeDetailsByAdmin(Long employeeId, AdminEmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원 ID입니다."));

        if (request.getName() != null) {
            employee.setEmployeeName(request.getName());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서 ID입니다."));
            employee.setDepartment(department);
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getRole() != null) {
            employee.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        return employeeRepository.save(employee);
    }

    /**
     * [직원 기능] 첫 로그인 시 비밀번호를 변경합니다.
     *
     * @param userEmail 현재 로그인된 사용자 이메일
     * @param request   새 비밀번호 정보 DTO
     */
    public void forcePasswordChange(String userEmail, PasswordChangeRequest request) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("인증된 사용자를 찾을 수 없습니다."));

        employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
    }
}
