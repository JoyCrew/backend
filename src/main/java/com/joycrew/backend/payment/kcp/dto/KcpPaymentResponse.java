package com.joycrew.backend.payment.kcp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KcpPaymentResponse {

    private boolean success;
    private String transactionId;
    private String resultCode;
    private String resultMsg;
}
