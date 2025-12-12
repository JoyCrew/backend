package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kyc.EmailsByPhoneResponse;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.service.KycTokenService;
import com.joycrew.backend.util.EmailMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/accounts/emails")
@RequiredArgsConstructor
public class AccountLookupController {

    private final KycTokenService kycTokenService;
    private final EmployeeRepository employeeRepo;

    @GetMapping("/by-phone")
    public ResponseEntity<EmailsByPhoneResponse> emailsByPhone(
            @RequestHeader("x-kyc-token") String kycToken) {

        // 1. 토큰에서 폰번호 추출 (하이픈이 있을 수도 있음)
        String rawPhone = kycTokenService.validateAndExtractPhone(kycToken);

        // 숫자 이외의 문자(하이픈 등) 제거 -> "01044907174" 형태로 정규화
        String cleanPhone = rawPhone.replaceAll("\\D", "");

        log.info("Email Lookup Request - Raw: {}, Clean: {}", rawPhone, cleanPhone);

        // 2. 정제된 번호(cleanPhone)로 DB 조회
        // EmployeeRepository.findByPhoneNumber(cleanPhone) 가 List<Employee> 라고 가정
        List<String> emails = employeeRepo.findByPhoneNumber(cleanPhone).stream()
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
