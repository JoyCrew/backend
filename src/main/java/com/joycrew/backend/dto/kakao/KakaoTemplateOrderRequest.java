package com.joycrew.backend.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record KakaoTemplateOrderRequest(
        @JsonProperty("template_token") String templateToken,
        @JsonProperty("receiver_type") String receiverType, // "PHONE"
        @JsonProperty("receivers") List<Map<String, Object>> receivers, // [{ "receiver_id": "010..." }]
        @JsonProperty("success_callback_url") String successCallbackUrl,
        @JsonProperty("fail_callback_url") String failCallbackUrl,
        @JsonProperty("gift_callback_url") String giftCallbackUrl, // 명세에 맞춰 이름 확인
        @JsonProperty("template_order_name") String templateOrderName,
        @JsonProperty("external_order_id") String externalOrderId
) {}
