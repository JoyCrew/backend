package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.exception.BillingRequiredException;
import com.joycrew.backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BillingGate {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public void requireBillingReady(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        if (!company.isBillingReady()) {
            throw new BillingRequiredException();
        }
    }
}
