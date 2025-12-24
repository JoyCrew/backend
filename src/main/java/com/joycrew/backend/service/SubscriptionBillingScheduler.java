package com.joycrew.backend.service;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBillingScheduler {

    private final CompanyRepository companyRepository;
    private final SubscriptionBillingService subscriptionBillingService;

    @Scheduled(cron = "0 0 3 * * *")
    public void autoBillingJob() {
        LocalDateTime now = LocalDateTime.now();
        List<Company> targets = companyRepository.findAutoBillingTargets(now);

        log.info("[AUTO-BILLING] started at {}, targets={}", now, targets.size());

        for (Company c : targets) {
            try {
                subscriptionBillingService.billCompany(c.getCompanyId());
            } catch (Exception e) {
                log.error("[AUTO-BILL-ERROR] companyId={} error={}", c.getCompanyId(), e.getMessage(), e);
            }
        }

        log.info("[AUTO-BILLING] finished at {}", LocalDateTime.now());
    }
}
