package com.joycrew.backend.repository;

import com.joycrew.backend.entity.CompanySubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySubscriptionPaymentRepository
        extends JpaRepository<CompanySubscriptionPayment, Long> {
}
