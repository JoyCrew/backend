package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByEmployee(Employee employee);
}
