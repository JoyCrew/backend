package com.joycrew.backend.service;

import com.joycrew.backend.dto.SubscriptionPaymentHistoryItem;
import com.joycrew.backend.dto.SubscriptionPaymentHistoryResponse;
import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.entity.enums.PaymentStatus;
import com.joycrew.backend.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionPaymentQueryService {

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;

    public SubscriptionPaymentHistoryResponse getHistory(Long companyId, int page, int size, PaymentStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));

        Page<SubscriptionPayment> result = (status == null)
                ? subscriptionPaymentRepository.findByCompany_CompanyIdOrderByRequestedAtDesc(companyId, pageable)
                : subscriptionPaymentRepository.findByCompany_CompanyIdAndStatusOrderByRequestedAtDesc(companyId, status, pageable);

        List<SubscriptionPaymentHistoryItem> items =
                result.getContent().stream().map(SubscriptionPaymentHistoryItem::from).toList();

        return new SubscriptionPaymentHistoryResponse(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
