package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 주문자 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  /** 상품 ID (카카오 템플릿 ID를 해시 변환해서 저장) */
  private Long productId;

  /** 상품 이름 */
  private String productName;

  /** 단가(포인트 단위) */
  private Integer productUnitPrice;

  /** 수량 */
  private Integer quantity;

  /** 총 금액(포인트 단위) */
  private Integer totalPrice;

  /** 주문 상태 */
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  /** 주문 일시 */
  private LocalDateTime orderedAt;

  /**
   * 외부 주문번호 (카카오 GiftBiz external_order_id)
   * - 카카오 스펙: length ≤ 70
   * - 성공 건은 중복 불가
   */
  @Column(name = "external_order_id", length = 70, unique = true, nullable = false)
  private String externalOrderId;
}
