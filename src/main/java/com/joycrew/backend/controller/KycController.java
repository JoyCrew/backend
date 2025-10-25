package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kyc.*;
import com.joycrew.backend.service.PhoneVerificationService;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.util.EmailMasker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/kyc/phone")
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

        // 2) 해당 phone으로 직원들 조회
        var employees = employeeRepo.findByPhoneNumber(r.phone());

        // 3) [수정된 로직]
        // Employee 엔티티를 VerifiedEmailInfo DTO 리스트로 변환
        // (회사 이메일과 개인 이메일을 별도 항목으로 취급)
        List<VerifiedEmailInfo> accounts = employees.stream()
                .flatMap(e -> Stream.of(
                        // 회사 이메일 정보
                        new VerifiedEmailInfo(EmailMasker.mask(e.getEmail()), e.getLastLoginAt()),
                        // 개인 이메일 정보 (null이 아닐 경우에만 생성)
                        (e.getPersonalEmail() != null)
                                ? new VerifiedEmailInfo(EmailMasker.mask(e.getPersonalEmail()), e.getLastLoginAt())
                                : null
                ))
                .filter(Objects::nonNull) // personalEmail이 null이었던 스트림 제거
                .filter(info -> info.email() != null) // 마스킹된 이메일이 null이 아닌 경우
                .distinct() // (이메일, 날짜)가 완전히 동일한 경우 중복 제거
                // 최근 로그인 날짜 기준으로 내림차순 정렬 (프론트 편의성)
                .sorted(Comparator.comparing(VerifiedEmailInfo::recentLoginAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        // 4) [수정된 생성자 호출] (3-인수)
        return new PhoneVerifyResponse(r.verified(), r.kycToken(), accounts);
    }
}

