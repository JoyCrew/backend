package com.joycrew.backend.repository;

import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    Optional<SubscriptionPayment> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    Page<SubscriptionPayment> findByCompany_CompanyIdOrderByRequestedAtDesc(Long companyId, Pageable pageable);

    Page<SubscriptionPayment> findByCompany_CompanyIdAndStatusOrderByRequestedAtDesc(Long companyId, PaymentStatus status, Pageable pageable);
}
