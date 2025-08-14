package com.joycrew.backend.entity.enums;

public enum OrderStatus {
  PLACED,       // 주문 생성(결제 완료)
  SHIPPED,      // 배송 중
  DELIVERED,    // 배송 완료
  CANCELED      // 취소(선택)
}
