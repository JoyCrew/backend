package com.joycrew.backend.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "payment.kcp")
public class KcpProperties {

    /**
     * 테스트 상점 코드 (예: T0000XXXX)
     */
    private String siteCd = "T1234TEST";

    /**
     * 테스트 사이트 키 (임의)
     */
    private String siteKey = "0123456789ABCDEF0123456789ABCDEF";

    /**
     * KCP 결제 요청 URL (테스트용)
     */
    private String payUrl = "https://testpay.kcp.co.kr/pay";

    public void setSiteCd(String siteCd) {
        this.siteCd = siteCd;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }
}
