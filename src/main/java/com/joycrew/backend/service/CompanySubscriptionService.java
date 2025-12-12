package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.CompanySubscriptionPayment;
import com.joycrew.backend.entity.enums.SubscriptionPaymentStatus;
import com.joycrew.backend.payment.kcp.KcpClient;
import com.joycrew.backend.payment.kcp.dto.KcpPaymentRequest;
import com.joycrew.backend.payment.kcp.dto.KcpPaymentResponse;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.CompanySubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanySubscriptionService {

    private static final int MONTHLY_PRICE = 50_000;

    private final CompanyRepository companyRepository;
    private final CompanySubscriptionPaymentRepository paymentRepository;
    private final KcpClient kcpClient;

    @Transactional
    public CompanySubscriptionPayment createPaymentRequest(Long companyId, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("months must be positive");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        int amount = MONTHLY_PRICE * months;

        String orderId = generateOrderId(company);

        CompanySubscriptionPayment payment = CompanySubscriptionPayment.builder()
                .company(company)
                .months(months)
                .amount(amount)
                .orderId(orderId)
                .status(SubscriptionPaymentStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * 실제 결제 요청 (KCP 클라이언트 호출)
     */
    @Transactional
    public CompanySubscriptionPayment requestAndProcessPayment(Long paymentId) {
        CompanySubscriptionPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        Company company = payment.getCompany();

        // 1) KCP 요청 DTO 만들기
        KcpPaymentRequest request = KcpPaymentRequest.builder()
                .siteCd("T1234TEST")   // properties에서 받아와도 됨
                .siteKey("0123456789ABCDEF0123456789ABCDEF")
                .orderId(payment.getOrderId())
                .goodName("JoyCrew Subscription")
                .amount(payment.getAmount())
                .currency("410") // KRW
                .buyerName(company.getCompanyName())
                .buyerEmail("admin@" + company.getCompanyName() + ".com") // TODO: 실제 admin 이메일 사용
                .build();

        // 2) KCP 호출
        KcpPaymentResponse response = kcpClient.requestPayment(request);

        // 3) 결과 반영
        if (response.isSuccess()) {
            payment.markSuccess(response.getTransactionId());
            // 회사 구독 만료일 연장
            company.extendSubscription(payment.getMonths());
        } else {
            payment.markFail(response.getResultCode() + " : " + response.getResultMsg());
        }

        return payment;
    }

    private String generateOrderId(Company company) {
        return "JOYCREW-" + company.getCompanyId() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
