package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  Optional<Company> findByCompanyName(String name);
  @Query("""
        SELECT c FROM Company c
        WHERE c.autoRenew = true
          AND c.tossBillingKey IS NOT NULL
          AND c.subscriptionEndAt IS NOT NULL
          AND c.subscriptionEndAt <= :now
        """)
  List<Company> findAutoBillingTargets(LocalDateTime now);
}
