package com.joycrew.backend.repository;

import com.joycrew.backend.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    Optional<PhoneVerification> findByRequestId(String requestId);
}