package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.RewardPointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RewardPointTransactionRepository extends JpaRepository<RewardPointTransaction, Long> {
  @EntityGraph(attributePaths = {"sender", "receiver"})
  List<RewardPointTransaction> findBySenderOrReceiverOrderByTransactionDateDesc(Employee sender, Employee receiver);

  List<RewardPointTransaction> findAllByOrderByTransactionDateDesc();

  @EntityGraph(attributePaths = {"sender", "receiver"})
  List<RewardPointTransaction> findBySenderOrReceiver(Employee sender, Employee receiver);

  Page<RewardPointTransaction> findBySenderCompanyCompanyIdOrReceiverCompanyCompanyId(Long companyId1, Long companyId2, Pageable pageable);

  @Query("""
    select tx
    from RewardPointTransaction tx
      left join fetch tx.sender s
      left join fetch tx.receiver r
    where
      (s.company.companyId = :companyId) or (r.company.companyId = :companyId)
    order by tx.transactionDate desc
  """)
  List<RewardPointTransaction> findAllByCompanyScope(@Param("companyId") Long companyId);
}