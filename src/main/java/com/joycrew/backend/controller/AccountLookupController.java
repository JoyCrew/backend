package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kyc.EmailsByPhoneResponse;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.service.KycTokenService;
import com.joycrew.backend.util.EmailMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/accounts/emails")
@RequiredArgsConstructor
public class AccountLookupController {

    private final KycTokenService kycTokenService;
    private final EmployeeRepository employeeRepo;

    @GetMapping("/by-phone")
    public ResponseEntity<EmailsByPhoneResponse> emailsByPhone(
            @RequestHeader("x-kyc-token") String kycToken) {

        String phone = kycTokenService.validateAndExtractPhone(kycToken);

        // EmployeeRepository.findByPhoneNumber(phone) 가 List<Employee> 라고 가정
        List<String> emails = employeeRepo.findByPhoneNumber(phone).stream()
                .flatMap(e -> Stream.of(e.getEmail(), e.getPersonalEmail()))
                .filter(Objects::nonNull)
                .map(EmailMasker::mask)
                .distinct()
                .toList();

        int count = emails.size();
        String message = (count == 0) ? "등록된 이메일이 없습니다." : null;

        return ResponseEntity.ok(new EmailsByPhoneResponse(
                true,
                count,
                emails,
                message
        ));
    }
}
