package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_recipient_created_at", columnList = "recipient_id, created_at DESC"),
        @Index(name = "idx_notification_unread", columnList = "recipient_id, is_read")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 받는 사람 (선물 수신자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipient;

    // 선물 보낸 사람 (선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private Employee actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title", length = 100)
    private String title; // 예: "동료로부터 포인트 선물!"

    @Lob
    @Column(name = "content")
    private String content; // 예: "홍길동님이 50P와 메시지를 보냈어요: 수고했어요!"

    @Column(name = "point_amount")
    private Integer pointAmount;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // 프론트 토스트용 TTL(예: 생성 후 24시간)

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusHours(24);
        }
    }

    public void markRead() { this.read = true; }
}
