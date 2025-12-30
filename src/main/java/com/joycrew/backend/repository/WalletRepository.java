package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // ⭐ 비관적 락 적용 (동시성 문제 해결용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.employee.employeeId = :employeeId")
    Optional<Wallet> findByEmployee_EmployeeIdForUpdate(Long employeeId);

    // 일반 조회는 락 없이 사용
    Optional<Wallet> findByEmployee_EmployeeId(Long employeeId);

    Optional<Wallet> findByEmployeeCompanyCompanyIdAndEmployeeEmployeeId(Long companyId, Long employeeId);
}
