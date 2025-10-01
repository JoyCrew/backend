package com.joycrew.backend.dto.kakao;

import java.util.List;
import java.util.Map;

/** https://gateway-giftbiz.kakao.com/openapi/giftbiz/v3/template/order/gift 바디 포맷 */
public record KakaoGiftDetailV3Request(
        List<Map<String, Object>> params // 주문 완료 후 수신자 단위 식별 파라미터
) {}