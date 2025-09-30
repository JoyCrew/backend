package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kyc.PhoneStartRequest;
import com.joycrew.backend.dto.kyc.PhoneStartResponse;
import com.joycrew.backend.dto.kyc.PhoneVerifyRequest;
import com.joycrew.backend.dto.kyc.PhoneVerifyResponse;
import com.joycrew.backend.service.PhoneVerificationService;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.util.EmailMasker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/kyc/phone")
@RequiredArgsConstructor
public class KycController {
    private final PhoneVerificationService svc;
    private final EmployeeRepository employeeRepo;

    @PostMapping("/start")
    public PhoneStartResponse start(@RequestBody @Valid PhoneStartRequest req) {
        var r = svc.start(req.phone());
        return new PhoneStartResponse(r.requestId(), r.resendAvailableInSec());
    }

    @PostMapping("/verify")
    public PhoneVerifyResponse verify(@RequestBody @Valid PhoneVerifyRequest req) {
        // 1) 코드 검증 + KYC 토큰 생성 + phone 획득
        var r = svc.verify(req.requestId(), req.code());

        // 2) 해당 phone으로 직원들 조회 → 이메일/최근로그인 추출
        var employees = employeeRepo.findByPhoneNumber(r.phone());

        List<String> emails = employees.stream()
                .flatMap(e -> Stream.of(e.getEmail(), e.getPersonalEmail()))
                .filter(Objects::nonNull)
                .map(EmailMasker::mask)
                .distinct()
                .toList();

        LocalDateTime recent = employees.stream()
                .map(e -> e.getLastLoginAt())     // LocalDateTime
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String recentStr = (recent != null) ? recent.toString() : null; // ISO-8601 문자열

        // 3) 응답
        return new PhoneVerifyResponse(r.verified(), r.kycToken(), emails, recentStr);
    }
}
