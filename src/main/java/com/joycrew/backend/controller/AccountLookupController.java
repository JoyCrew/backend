package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kyc.EmailsByPhoneResponse;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.service.KycTokenService;
import com.joycrew.backend.util.EmailMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 확인용 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j // 로그를 찍어보시려면 추가하세요
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

        // 🚨 [수정 핵심] 숫자 이외의 문자(하이픈 등) 제거 -> "01044907174"
        String cleanPhone = rawPhone.replaceAll("\\D", "");

        log.info("Email Lookup Request - Raw: {}, Clean: {}", rawPhone, cleanPhone);

        // 2. 정제된 번호(cleanPhone)로 DB 조회
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