package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  List<Department> findAllByCompanyCompanyId(Long companyId);
  Optional<Department> findByCompanyAndName(Company company, String name);
}