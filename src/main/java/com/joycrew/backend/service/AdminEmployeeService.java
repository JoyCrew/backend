package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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

        Company company = companyRepository.findByCompanyName(request.companyName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사명입니다."));

        Department department = null;
        if (request.departmentName() != null && !request.departmentName().isBlank()) {
            department = departmentRepository.findByCompanyAndName(company, request.departmentName())
                    .orElseThrow(() -> new IllegalArgumentException("해당 회사에 존재하지 않는 부서명입니다."));
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

    public void registerEmployeesFromCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length < 7) {
                    log.warn("누락된 필드가 있는 행 건너뜀: {}", line);
                    continue;
                }

                try {
                    EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                            tokens[0].trim(), // name
                            tokens[1].trim(), // email
                            tokens[2].trim(), // password
                            tokens[3].trim(), // companyName
                            tokens[4].trim().isBlank() ? null : tokens[4].trim(), // departmentName (nullable)
                            tokens[5].trim(), // position
                            UserRole.valueOf(tokens[6].trim().toUpperCase()) // role
                    );
                    registerEmployee(request);
                } catch (Exception e) {
                    log.warn("직원 등록 실패 - 입력값: [{}], 사유: {}", line, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        }
    }
}
