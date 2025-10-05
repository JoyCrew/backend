package com.joycrew.backend.entity.kakao;

import com.joycrew.backend.entity.enums.GiftCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoTemplate {

    @Id
    @Column(length = 64)
    private String templateId;              // 내부 식별자(프론트 노출 ID)

    @Column(nullable = false, length = 128, unique = true)
    private String templateToken;           // 카카오 발송용 template_token

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 128)
    private String brand;

    @Column(nullable = false)
    private Integer basePriceKrw;

    @Column(length = 512)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private GiftCategory joyCategory;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
