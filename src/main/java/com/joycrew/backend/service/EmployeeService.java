package com.joycrew.backend.service;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.dto.UserProfileUpdateRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final S3FileStorageService s3FileStorageService;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userEmail) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));

        Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                .orElse(new Wallet(employee));

        return employeeMapper.toUserProfileResponse(employee, wallet);
    }

    public void forcePasswordChange(String userEmail, PasswordChangeRequest request) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));
        employee.changePassword(request.newPassword(), passwordEncoder);
    }

    public void updateUserProfile(String userEmail, UserProfileUpdateRequest request, MultipartFile profileImage) {
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));

        if (request.name() != null) {
            employee.updateName(request.name());
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImageUrl = s3FileStorageService.uploadFile(profileImage);
            employee.updateProfileImageUrl(profileImageUrl);
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
        // No explicit save call is needed due to @Transactional
    }
}