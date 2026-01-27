package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        Integer pointAmount,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long actorId,
        String actorEmail,
        String actorName,
        Long recipientId
) {}
