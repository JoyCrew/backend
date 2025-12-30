package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  List<Department> findAllByCompanyCompanyId(Long companyId);

  Optional<Department> findByCompanyAndName(Company company, String name);

  Page<Department> findByCompanyCompanyId(Long companyId, Pageable pageable);

  Optional<Department> findByCompanyCompanyIdAndDepartmentId(Long companyId, Long departmentId);

  Optional<Department> findByCompanyCompanyIdAndName(Long companyId, String name);

  boolean existsByCompanyCompanyIdAndName(Long companyId, String name);
}
