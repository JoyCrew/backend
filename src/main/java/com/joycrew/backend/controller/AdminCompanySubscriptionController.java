package com.joycrew.backend.controller;

import com.joycrew.backend.dto.SubscriptionRequestDto;
import com.joycrew.backend.entity.CompanySubscriptionPayment;
import com.joycrew.backend.service.CompanySubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanySubscriptionController {

    private final CompanySubscriptionService subscriptionService;

    /**
     * 1단계: admin이 “N개월 연장” 신청 → payment row 생성
     */
    @PostMapping("/{companyId}/subscription/payment")
    public ResponseEntity<CompanySubscriptionPayment> createSubscriptionPayment(
            @PathVariable Long companyId,
            @RequestBody SubscriptionRequestDto request
    ) {
        CompanySubscriptionPayment payment =
                subscriptionService.createPaymentRequest(companyId, request.getMonths());
        return ResponseEntity.ok(payment);
    }

    /**
     * 2단계: (예시)
     * 특정 paymentId에 대해 실제 KCP 결제 요청 & 처리
     * - 실제 서비스에서는 프런트에서 KCP 결제창으로 리다이렉트/스크립트 처리 후
     *   callback을 받는 구조로 바꾸면 됨.
     */
    @PostMapping("/subscription/payment/{paymentId}/execute")
    public ResponseEntity<CompanySubscriptionPayment> executeSubscriptionPayment(
            @PathVariable Long paymentId
    ) {
        CompanySubscriptionPayment updated = subscriptionService.requestAndProcessPayment(paymentId);
        return ResponseEntity.ok(updated);
    }
}
