package com.joycrew.backend.payment.kcp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KcpPaymentRequest {

    private String siteCd;
    private String siteKey;

    private String orderId;
    private String goodName;
    private int amount; // KRW
    private String currency; // "410" (KRW) 등

    private String buyerName;
    private String buyerEmail;

    // 기타 KCP가 요구하는 값들..
}
