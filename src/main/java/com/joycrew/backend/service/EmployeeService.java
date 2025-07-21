package com.joycrew.backend.service;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerEmployee(String email, String rawPassword, String name) {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Employee newEmployee = Employee.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .employeeName(name)
                .status("ACTIVE")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        employeeRepository.save(newEmployee);
    }
}
