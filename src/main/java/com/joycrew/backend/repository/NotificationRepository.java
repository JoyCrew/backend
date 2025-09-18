package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Notification;
import com.joycrew.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByRecipientAndReadFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            Employee recipient, LocalDateTime now);

    List<Notification> findTop100ByRecipientAndCreatedAtAfterOrderByCreatedAtDesc(
            Employee recipient, LocalDateTime after);
}
