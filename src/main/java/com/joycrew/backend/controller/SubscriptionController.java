package com.joycrew.backend.controller;

import com.joycrew.backend.dto.IssueBillingKeyRequest;
import com.joycrew.backend.dto.SubscriptionPaymentHistoryResponse;
import com.joycrew.backend.dto.SubscriptionSummaryResponse;
import com.joycrew.backend.dto.SuccessResponse;
import com.joycrew.backend.entity.enums.PaymentStatus;
import com.joycrew.backend.service.SubscriptionBillingKeyAppService;
import com.joycrew.backend.service.SubscriptionPaymentQueryService;
import com.joycrew.backend.service.SubscriptionQueryService;
import com.joycrew.backend.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionBillingKeyAppService billingKeyAppService;
    private final SubscriptionPaymentQueryService paymentQueryService;
    private final SubscriptionQueryService subscriptionQueryService;

    /** ✅ authKey만 받음 */
    @PostMapping("/billing-key/issue")
    public ResponseEntity<SuccessResponse> issueBillingKey(@RequestBody IssueBillingKeyRequest req) {
        Long companyId = Tenant.id();
        billingKeyAppService.issueAndSaveBillingKey(companyId, req.authKey());
        return ResponseEntity.ok(new SuccessResponse("BillingKey issued and auto-renew enabled"));
    }

    /** 구독 해지(자동결제 OFF) */
    @PostMapping("/auto/disable")
    public ResponseEntity<SuccessResponse> disableAutoRenew() {
        Long companyId = Tenant.id();
        billingKeyAppService.disableAutoRenew(companyId);
        return ResponseEntity.ok(new SuccessResponse("Auto-renew disabled"));
    }

    /** ✅ 결제 이력 조회(관리자 페이지) */
    @GetMapping("/payments")
    public ResponseEntity<SubscriptionPaymentHistoryResponse> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentStatus status
    ) {
        Long companyId = Tenant.id();
        return ResponseEntity.ok(paymentQueryService.getHistory(companyId, page, size, status));
    }

    @GetMapping("/summary")
    public ResponseEntity<SubscriptionSummaryResponse> getSubscriptionSummary() {
        Long companyId = Tenant.id();
        return ResponseEntity.ok(
                subscriptionQueryService.getSubscriptionSummary(companyId)
        );
    }
}
