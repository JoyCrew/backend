package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminEmployeeQueryResponse;
import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.AdminPagedEmployeeResponse;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeManagementService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final EmployeeMapper employeeMapper;
  @PersistenceContext
  private final EntityManager em;

  @Transactional(readOnly = true)
  public AdminPagedEmployeeResponse searchEmployees(String keyword, int page, int size) {
    StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");
    if (keyword != null && !keyword.isBlank()) {
      whereClause.append("AND (LOWER(e.employeeName) LIKE :keyword ")
          .append("OR LOWER(e.email) LIKE :keyword ")
          .append("OR LOWER(d.name) LIKE :keyword) ");
    }

    String countJpql = "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d " + whereClause;
    TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
    if (keyword != null && !keyword.isBlank()) {
      countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }
    long total = countQuery.getSingleResult();
    int totalPages = (int) Math.ceil((double) total / size);

    String dataJpql = "SELECT e FROM Employee e " +
        "LEFT JOIN FETCH e.department d " +
        "LEFT JOIN FETCH e.company c " +
        whereClause +
        "ORDER BY e.employeeName ASC";
    TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class)
        .setFirstResult(page * size)
        .setMaxResults(size);
    if (keyword != null && !keyword.isBlank()) {
      dataQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }

    List<AdminEmployeeQueryResponse> employees = dataQuery.getResultList().stream()
        .map(employeeMapper::toAdminEmployeeQueryResponse) // Use Mapper
        .toList();

    return new AdminPagedEmployeeResponse(
        employees,
        page, // Return 0-based page index for consistency
        totalPages,
        page >= totalPages - 1
    );
  }

  public Employee updateEmployee(Long employeeId, AdminEmployeeUpdateRequest request) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new UserNotFoundException("Employee not found with ID: " + employeeId));

    if (request.name() != null) {
      employee.updateName(request.name());
    }
    if (request.departmentId() != null) {
      Department department = departmentRepository.findById(request.departmentId())
          .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + request.departmentId()));
      employee.assignToDepartment(department);
    }
    if (request.position() != null) {
      employee.updatePosition(request.position());
    }
    if (request.level() != null) {
      employee.updateRole(request.level());
    }
    if (request.status() != null) {
      employee.updateStatus(request.status());
    }
    return employee; // @Transactional will handle the save
  }

  public void deactivateEmployee(Long employeeId) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new UserNotFoundException("Employee not found with ID: " + employeeId));
    employee.updateStatus("DELETED");
  }

  @Transactional(readOnly = true)
  public List<AdminEmployeeQueryResponse> getAllEmployees() {
    return employeeRepository.findAll().stream()
        .map(employeeMapper::toAdminEmployeeQueryResponse) // Use Mapper
        .toList();
  }
}