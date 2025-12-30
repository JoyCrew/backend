package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBillingService {

    private final CompanyRepository companyRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final TossBillingChargeService tossBillingChargeService;

    @Value("${subscription.monthly-price}")
    private long monthlyPrice; // ✅ 정확한 amount 저장용

    private static final DateTimeFormatter ORDER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public void billCompany(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();

        if (!company.canAutoBill()) {
            log.warn("[AUTO-BILL-SKIP] companyId={} cannot auto bill", companyId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 이번 결제가 커버할 구독 기간: (현재 subscriptionEndAt) ~ +1month
        LocalDateTime periodStart = (company.getSubscriptionEndAt() != null) ? company.getSubscriptionEndAt() : now;
        LocalDateTime periodEnd = periodStart.plusMonths(1);

        // ✅ 멱등성 orderId: company + periodStart 기준 고정
        String orderId = generateOrderId(companyId, periodStart);

        // 이미 성공 이력이 있으면 재결제 방지
        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            if (existing.getStatus().name().equals("SUCCESS")) {
                log.info("[AUTO-BILL-SKIP] already success orderId={}", orderId);
                return;
            }
        });

        // ✅ PENDING 생성(없으면) + amount 정확히 저장
        SubscriptionPayment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> paymentRepository.save(
                        SubscriptionPayment.pending(
                                company,
                                orderId,
                                monthlyPrice,   // ✅ 여기!
                                periodStart,
                                periodEnd,
                                now
                        )
                ));

        // Toss 결제 호출
        TossBillingChargeService.TossChargeResult result =
                tossBillingChargeService.charge(company, orderId);

        if (result.success()) {
            payment.markSuccess(result.paymentKey(), result.approvedAt(), result.rawResponse());
            company.extendSubscription(1); // ✅ 성공 시 구독 연장

            log.info("[AUTO-BILL-SUCCESS] companyId={}, orderId={}, newEndAt={}",
                    companyId, orderId, company.getSubscriptionEndAt());
        } else {
            payment.markFailed(
                    (result.failCode() != null ? result.failCode() : "FAILED"),
                    result.rawResponse()
            );
            company.markFailed();

            log.error("[AUTO-BILL-FAILED] companyId={}, orderId={}, code={}, msg={}",
                    companyId, orderId, result.failCode(), result.failMessage());
        }
    }

    private String generateOrderId(Long companyId, LocalDateTime periodStart) {
        return "SUB_" + companyId + "_" + periodStart.format(ORDER_FMT);
    }
}
