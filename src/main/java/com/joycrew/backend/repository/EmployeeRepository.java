package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  Optional<Employee> findByEmail(String email);

  @Query("""
      SELECT e 
      FROM Employee e 
      WHERE e.employeeName LIKE %:keyword% 
         OR e.email LIKE %:keyword% 
         OR e.department.name LIKE %:keyword%
      """)
  Page<Employee> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

  @Query("""
      SELECT e 
      FROM Employee e 
      JOIN FETCH e.company 
      WHERE e.employeeId = :id
      """)
  Optional<Employee> findByIdWithCompany(@Param("id") Long id);

  @Query("""
    SELECT e
    FROM Employee e
    JOIN FETCH e.company c
    WHERE e.employeeId = :employeeId
        AND c.companyId = :companyId
    """)
  Optional<Employee> findByIdWithCompanyAndCompanyId(Long employeeId, Long companyId);

  List<Employee> findAllByCompanyCompanyIdAndEmployeeIdIn(Long companyId, List<Long> employeeIds);

  List<Employee> findAllByCompanyCompanyId(Long companyId);

  List<Employee> findByPhoneNumber(String phoneNumber);

  Page<Employee> findByCompanyCompanyIdAndStatus(Long companyId, String status, Pageable pageable);

  Optional<Employee> findByCompanyCompanyIdAndEmail(Long companyId, String email);

  Optional<Employee> findByCompanyCompanyIdAndEmployeeId(Long companyId, Long employeeId);

  boolean existsByCompanyCompanyIdAndEmail(Long companyId, String email);

  @Query("""
    SELECT e
    FROM Employee e
    JOIN FETCH e.company c
    WHERE c.companyId = :companyId
        AND e.employeeId = :employeeId
    """)
  Optional<Employee> findByCompanyCompanyIdAndEmployeeIdWithCompany(Long companyId, Long employeeId);

}
