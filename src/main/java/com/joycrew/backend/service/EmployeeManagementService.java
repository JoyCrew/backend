package com.joycrew.backend.service;

import com.joycrew.backend.dto.AdminEmployeeQueryResponse;
import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.AdminPagedEmployeeResponse;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.exception.BillingRequiredException;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import com.joycrew.backend.tenant.Tenant;
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
  private final CompanyRepository companyRepository;
  private final EmployeeMapper employeeMapper;

  @PersistenceContext
  private final EntityManager em;

  /** 카드 등록 게이트(직원 관리/등록/조회 모두 차단) */
  private Company requireBillingReady() {
    Long companyId = Tenant.id();
    Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalStateException("Company not found (tenant=" + companyId + ")"));

    if (!company.isBillingReady()) {
      throw new BillingRequiredException();
    }
    return company;
  }

  @Transactional(readOnly = true)
  public AdminPagedEmployeeResponse searchEmployees(String keyword, int page, int size) {
    requireBillingReady();

    Long companyId = Tenant.id();

    StringBuilder whereClause = new StringBuilder("WHERE c.companyId = :companyId ");
    if (keyword != null && !keyword.isBlank()) {
      whereClause.append("AND (LOWER(e.employeeName) LIKE :keyword ")
              .append("OR LOWER(e.email) LIKE :keyword ")
              .append("OR LOWER(d.name) LIKE :keyword) ");
    }

    String countJpql =
            "SELECT COUNT(e) " +
                    "FROM Employee e " +
                    "JOIN e.company c " +
                    "LEFT JOIN e.department d " +
                    whereClause;

    TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
    countQuery.setParameter("companyId", companyId);
    if (keyword != null && !keyword.isBlank()) {
      countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }

    long total = countQuery.getSingleResult();
    int totalPages = (int) Math.ceil((double) total / size);

    String dataJpql =
            "SELECT e " +
                    "FROM Employee e " +
                    "JOIN FETCH e.company c " +
                    "LEFT JOIN FETCH e.department d " +
                    whereClause +
                    "ORDER BY e.employeeName ASC";

    TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class)
            .setFirstResult(page * size)
            .setMaxResults(size);

    dataQuery.setParameter("companyId", companyId);
    if (keyword != null && !keyword.isBlank()) {
      dataQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }

    List<AdminEmployeeQueryResponse> employees = dataQuery.getResultList().stream()
            .map(employeeMapper::toAdminEmployeeQueryResponse)
            .toList();

    return new AdminPagedEmployeeResponse(
            employees,
            page,
            totalPages,
            page >= totalPages - 1
    );
  }

  public Employee updateEmployee(Long employeeId, AdminEmployeeUpdateRequest request) {
    requireBillingReady();

    Long companyId = Tenant.id();

    Employee employee = employeeRepository.findByCompanyCompanyIdAndEmployeeId(companyId, employeeId)
            .orElseThrow(() -> new UserNotFoundException("Employee not found with ID: " + employeeId));

    if (request.name() != null) {
      employee.updateName(request.name());
    }

    if (request.departmentId() != null) {
      Department department = departmentRepository.findByCompanyCompanyIdAndDepartmentId(companyId, request.departmentId())
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

    return employee; // dirty checking
  }

  public void deactivateEmployee(Long employeeId) {
    requireBillingReady();

    Long companyId = Tenant.id();

    Employee employee = employeeRepository.findByCompanyCompanyIdAndEmployeeId(companyId, employeeId)
            .orElseThrow(() -> new UserNotFoundException("Employee not found with ID: " + employeeId));

    employee.updateStatus("INACTIVE");
  }

  @Transactional(readOnly = true)
  public List<AdminEmployeeQueryResponse> getAllEmployees() {
    requireBillingReady();

    Long companyId = Tenant.id();

    return employeeRepository.findAllByCompanyCompanyId(companyId).stream()
            .map(employeeMapper::toAdminEmployeeQueryResponse)
            .toList();
  }
}
