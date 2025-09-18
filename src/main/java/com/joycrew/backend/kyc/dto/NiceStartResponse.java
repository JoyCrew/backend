package com.joycrew.backend.kyc.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class NiceStartResponse {
    private String tokenVersionId;
    private String encData;
    private String integrityValue;
    private String requestNo;
    private String standardWindowUrl;
}
