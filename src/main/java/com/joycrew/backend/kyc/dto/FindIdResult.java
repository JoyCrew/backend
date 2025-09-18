package com.joycrew.backend.kyc.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class FindIdResult {
    private boolean matched;
    private String maskedLoginId; // 이메일 아이디를 마스킹해 반환
}
