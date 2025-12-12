package com.joycrew.backend.payment.kcp;

import com.joycrew.backend.config.KcpProperties;
import com.joycrew.backend.payment.kcp.dto.KcpPaymentRequest;
import com.joycrew.backend.payment.kcp.dto.KcpPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KcpClient {

    private final KcpProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public KcpPaymentResponse requestPayment(KcpPaymentRequest request) {
        // TODO: NHN KCP 실제 연동 스펙에 맞게 구현
        // 여기서는 "성공했다고 치자" 형태의 stub

        // 예시 형태:
        // ResponseEntity<KcpPaymentResponse> response =
        //    restTemplate.postForEntity(properties.getPayUrl(), request, KcpPaymentResponse.class);

        // 테스트용 더미 응답
        return new KcpPaymentResponse(
                true,
                "TID-TEST-" + request.getOrderId(),
                "0000",
                "SUCCESS"
        );
    }
}
