package com.joycrew.backend.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    BEAUTY("뷰티"),
    APPLIANCES("가전"),
    FURNITURE("가구"),
    CLOTHING("옷"),
    FOOD("음식");

    private final String kr; // 실제 쿠팡 검색에 사용할 한글 키워드
}