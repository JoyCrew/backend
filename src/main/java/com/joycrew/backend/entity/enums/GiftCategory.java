package com.joycrew.backend.entity.enums;

import lombok.Getter;

@Getter
public enum GiftCategory {
    // 모바일 교환권
    CAFE("카페"),
    VOUCHER("상품권"),
    CHICKEN_PIZZA_BURGER("치킨/피자/버거"),
    BAKERY_DONUT("베이커리/도넛"),
    ICE_CREAM("아이스크림"),
    CONVENIENCE_STORE("편의점"),
    DINING_MEALKIT("외식/간편식"),
    LIFESTYLE_MISC("생활편의/기타"),
    HOTEL_MEAL("호텔 식사권");

    private final String koName;
    GiftCategory(String koName) { this.koName = koName; }
}
