package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionBillingKeyAppService {

    private final CompanyRepository companyRepository;
    private final TossBillingKeyService tossBillingKeyService;

    @Transactional
    public void issueAndSaveBillingKey(Long companyId, String authKey) {
        Company company = companyRepository.findById(companyId).orElseThrow();

        String customerKey = "company_" + companyId;
        String billingKey = tossBillingKeyService.issueBillingKey(authKey, customerKey);

        company.registerBillingKeyAndEnableAutoRenew(billingKey, customerKey);
        company.initializeSubscriptionEndAtIfFirstTime(); // ✅ now + 1개월
    }

    @Transactional
    public void disableAutoRenew(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        company.disableAutoRenew();
    }
}
