package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardPointTransactionRepository extends JpaRepository<RewardPointTransaction, Long> {
  @EntityGraph(attributePaths = {"sender", "receiver"})
  List<RewardPointTransaction> findBySenderOrReceiverOrderByTransactionDateDesc(Employee sender, Employee receiver);

  List<RewardPointTransaction> findAllByOrderByTransactionDateDesc();

  @EntityGraph(attributePaths = {"sender", "receiver"})
  List<RewardPointTransaction> findBySenderOrReceiver(Employee sender, Employee receiver);
}