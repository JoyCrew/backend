package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBillingScheduler {

    private final CompanyRepository companyRepository;
    private final SubscriptionBillingService billingService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void autoBillingJob() {
        LocalDateTime now = LocalDateTime.now();
        List<Company> targets = companyRepository.findAutoBillingTargets(now);

        for (Company c : targets) {
            try {
                billingService.billCompany(c.getCompanyId());
            } catch (Exception e) {
                log.error("[AUTO-BILL-ERROR] companyId={}", c.getCompanyId(), e);
            }
        }
    }
}
