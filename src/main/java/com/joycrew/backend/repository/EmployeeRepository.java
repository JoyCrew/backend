package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @EntityGraph(attributePaths = {"company", "department"})
    Optional<Employee> findByEmail(String email);
}