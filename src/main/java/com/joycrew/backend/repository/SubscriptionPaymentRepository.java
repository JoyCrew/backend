package com.joycrew.backend.repository;

import com.joycrew.backend.entity.SubscriptionPayment;
import com.joycrew.backend.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    Optional<SubscriptionPayment> findByOrderId(String orderId);

    Page<SubscriptionPayment> findByCompanyCompanyId(Long companyId, Pageable pageable);

    Page<SubscriptionPayment> findByCompanyCompanyIdAndStatus(Long companyId, PaymentStatus status, Pageable pageable);

    // ✅ 전체 결제 이력 (최신순)
    Page<SubscriptionPayment> findByCompanyCompanyIdOrderByCreatedAtDesc(
            Long companyId,
            Pageable pageable
    );

    // ✅ 상태별 결제 이력 (최신순)
    Page<SubscriptionPayment> findByCompanyCompanyIdAndStatusOrderByCreatedAtDesc(
            Long companyId,
            PaymentStatus status,
            Pageable pageable
    );
}
