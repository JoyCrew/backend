package com.joycrew.backend.repository;

import com.joycrew.backend.entity.RecentProductView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecentProductViewRepository extends JpaRepository<RecentProductView, Long> {

  Optional<RecentProductView> findByEmployee_EmployeeIdAndProduct_Id(Long employeeId, Long productId);

  List<RecentProductView> findByEmployee_EmployeeIdAndViewedAtAfterOrderByViewedAtDesc(
      Long employeeId, LocalDateTime threshold, Pageable pageable
  );

  long deleteByViewedAtBefore(LocalDateTime threshold);
}
