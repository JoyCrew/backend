package com.joycrew.backend.service;

import com.joycrew.backend.dto.SubscriptionPaymentHistoryResponse;
import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.entity.enums.PaymentStatus;
import com.joycrew.backend.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentQueryService {

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;

    public SubscriptionPaymentHistoryResponse getHistory(
            Long companyId,
            int page,
            int size,
            PaymentStatus status
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<SubscriptionPayment> result =
                (status == null)
                        ? subscriptionPaymentRepository
                        .findByCompanyCompanyIdOrderByCreatedAtDesc(companyId, pageable)
                        : subscriptionPaymentRepository
                        .findByCompanyCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status, pageable);

        return SubscriptionPaymentHistoryResponse.from(result);
    }
}
