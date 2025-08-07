package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeRegistrationService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public Employee registerEmployee(EmployeeRegistrationRequest request) {
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("This email is already in use.");
        }

        Company company = companyRepository.findByCompanyName(request.companyName())
                .orElseThrow(() -> new IllegalArgumentException("Company with the given name does not exist."));

        Department department = null;
        if (request.departmentName() != null && !request.departmentName().isBlank()) {
            department = departmentRepository.findByCompanyAndName(company, request.departmentName())
                    .orElseThrow(() -> new IllegalArgumentException("Department with the given name does not exist in this company."));
        }

        Employee newEmployee = Employee.builder()
                .employeeName(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.initialPassword()))
                .company(company)
                .department(department)
                .position(request.position())
                .role(request.level())
                .status("ACTIVE")
                .birthday(request.birthday())
                .address(request.address())
                .hireDate(request.hireDate())
                .build();

        Employee savedEmployee = employeeRepository.save(newEmployee);
        walletRepository.save(new Wallet(savedEmployee));
        return savedEmployee;
    }

    public void registerEmployeesFromCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length < 10) { // Adjusted for all fields including optional ones
                    log.warn("Skipping row with missing fields: {}", line);
                    continue;
                }

                try {
                    AdminLevel adminLevel = parseAdminLevel(tokens[6].trim());
                    LocalDate birthday = parseDate(tokens[7].trim());
                    String address = tokens[8].trim();
                    LocalDate hireDate = parseDate(tokens[9].trim());

                    EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                            tokens[0].trim(), // name
                            tokens[1].trim(), // email
                            tokens[2].trim(), // initialPassword
                            tokens[3].trim(), // companyName
                            tokens[4].trim().isBlank() ? null : tokens[4].trim(), // departmentName
                            tokens[5].trim(), // position
                            adminLevel,
                            birthday,
                            address,
                            hireDate
                    );
                    registerEmployee(request);
                } catch (Exception e) {
                    log.warn("Failed to register employee. Input: [{}], Reason: {}", line, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file.", e);
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr); // Expects YYYY-MM-DD format
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}. Processing as null.", dateStr);
            return null;
        }
    }

    private AdminLevel parseAdminLevel(String level) {
        if (level == null || level.isBlank()) {
            return AdminLevel.EMPLOYEE;
        }
        try {
            return AdminLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role level: {}. Defaulting to EMPLOYEE.", level);
            return AdminLevel.EMPLOYEE;
        }
    }
}