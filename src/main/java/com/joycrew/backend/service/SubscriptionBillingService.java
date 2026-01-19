package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.entity.enums.PaymentStatus;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBillingService {

    private final CompanyRepository companyRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final TossBillingChargeService tossBillingChargeService;

    @Value("${subscription.monthly-price}")
    private long monthlyPrice;

    private static final DateTimeFormatter ORDER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public void billCompany(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();

        if (!company.canAutoBill()) {
            log.warn("[AUTO-BILL-SKIP] companyId={} cannot auto bill", companyId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 만료일 기준으로 다음 1개월 결제
        LocalDateTime periodStart = (company.getSubscriptionEndAt() != null) ? company.getSubscriptionEndAt() : now;
        LocalDateTime periodEnd = periodStart.plusMonths(1);

        String orderId = generateOrderId(companyId, periodStart);

        // ✅ 이미 SUCCESS면 진짜로 종료
        Optional<SubscriptionPayment> existingOpt = paymentRepository.findByOrderId(orderId);
        if (existingOpt.isPresent() && existingOpt.get().getStatus() == PaymentStatus.SUCCESS) {
            log.info("[AUTO-BILL-SKIP] already success orderId={}", orderId);
            return;
        }

        SubscriptionPayment payment = existingOpt.orElseGet(() ->
                paymentRepository.save(
                        SubscriptionPayment.pending(company, orderId, monthlyPrice, periodStart, periodEnd, now)
                )
        );

        TossBillingChargeService.TossChargeResult result =
                tossBillingChargeService.charge(company, orderId);

        if (result.success()) {
            payment.markSuccess(result.paymentKey(), result.approvedAt(), result.rawResponse());
            company.extendSubscription(1);

            log.info("[AUTO-BILL-SUCCESS] companyId={}, orderId={}, newEndAt={}",
                    companyId, orderId, company.getSubscriptionEndAt());
        } else {
            payment.markFailed(result.failCode() != null ? result.failCode() : "FAILED", result.rawResponse());
            company.markFailed();

            log.error("[AUTO-BILL-FAILED] companyId={}, orderId={}, code={}, msg={}",
                    companyId, orderId, result.failCode(), result.failMessage());
        }
    }

    private String generateOrderId(Long companyId, LocalDateTime periodStart) {
        return "SUB_" + companyId + "_" + periodStart.format(ORDER_FMT);
    }
}
