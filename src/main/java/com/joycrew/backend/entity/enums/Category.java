package com.joycrew.backend.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    BEAUTY("뷰티"),
    HOUSEHOLD("생활용품"),
    HOME_INTERIOR("홈인테리어"),
    STATIONERY_OFFICE("문구/오피스"),
    WOMEN_CLOTHING("여성복"),
    MEN_CLOTHING("남성복"),
    HEALTH_SUPPLEMENTS("헬스/건강식품");

    private final String kr; // 실제 쿠팡 검색에 사용할 한글 키워드
}
