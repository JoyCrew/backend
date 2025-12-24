package com.joycrew.backend.service;

import com.joycrew.backend.dto.SubscriptionSummaryResponse;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryService {

    private final CompanyRepository companyRepository;

    public SubscriptionSummaryResponse getSubscriptionSummary(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();

        return new SubscriptionSummaryResponse(
                company.getCreatedAt(),        // 가입일
                company.getSubscriptionEndAt(),// 다음 결제 예정일
                company.isAutoRenew(),
                company.getStatus()
        );
    }
}
