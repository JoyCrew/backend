package com.joycrew.backend.entity;

import com.joycrew.backend.entity.enums.Tag;
import com.joycrew.backend.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reward_point_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RewardPointTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long transactionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = true)
  private Employee sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = true)
  private Employee receiver;

  @Column(nullable = false)
  private Integer pointAmount;

  @Lob
  private String message;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  // A collection of tags associated with the transaction.
  @ElementCollection(targetClass = Tag.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "transaction_tags", joinColumns = @JoinColumn(name = "transaction_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "tag", nullable = false)
  private List<Tag> tags;

  @Column(nullable = false)
  private LocalDateTime transactionDate;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = this.transactionDate = LocalDateTime.now();
  }
}