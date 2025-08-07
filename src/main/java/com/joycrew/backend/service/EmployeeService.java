package com.joycrew.backend.service;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.dto.UserProfileUpdateRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
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
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userEmail) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));

        Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                .orElse(new Wallet(employee));

        return UserProfileResponse.from(employee, wallet);
    }

    public void forcePasswordChange(String userEmail, PasswordChangeRequest request) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));
        employee.changePassword(request.newPassword(), passwordEncoder);
    }

    public void updateUserProfile(String userEmail, UserProfileUpdateRequest request) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 찾을 수 없습니다."));

        if (request.name() != null) {
            employee.updateName(request.name());
        }
        if (request.profileImageUrl() != null) {
            employee.updateProfileImageUrl(request.profileImageUrl());
        }
        if (request.personalEmail() != null) {
            employee.updatePersonalEmail(request.personalEmail());
        }
        if (request.phoneNumber() != null) {
            employee.updatePhoneNumber(request.phoneNumber());
        }
        if (request.birthday() != null) {
            employee.updateBirthday(request.birthday());
        }
        if (request.address() != null) {
            employee.updateAddress(request.address());
        }
        employeeRepository.save(employee);
    }
}