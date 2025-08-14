package com.joycrew.backend.repository;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EmployeeQueryRepository {

  @PersistenceContext
  private final EntityManager em;
  private final EmployeeMapper employeeMapper;

  public PagedEmployeeResponse getEmployees(String keyword, int page, int size, Long currentEmployeeId) {
    StringBuilder whereClause = new StringBuilder();
    boolean hasKeyword = StringUtils.hasText(keyword);

    // Base condition to exclude the current user
    whereClause.append("WHERE e.id != :currentEmployeeId ");

    // Dynamic condition for keyword search
    if (hasKeyword) {
      whereClause.append("AND (LOWER(e.employeeName) LIKE :keyword ")
          .append("OR LOWER(e.email) LIKE :keyword ")
          .append("OR LOWER(e.department.name) LIKE :keyword) ");
    }

    // Count Query
    String countJpql = "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d " + whereClause;
    TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
    countQuery.setParameter("currentEmployeeId", currentEmployeeId);
    if (hasKeyword) {
      countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }
    long totalCount = countQuery.getSingleResult();
    int totalPages = (int) Math.ceil((double) totalCount / size);

    // Data Query
    String dataJpql = "SELECT e FROM Employee e " +
        "LEFT JOIN FETCH e.department d " +
        whereClause +
        "ORDER BY e.employeeName ASC";
    TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class);
    dataQuery.setParameter("currentEmployeeId", currentEmployeeId);
    if (hasKeyword) {
      dataQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }
    dataQuery.setFirstResult(page * size);
    dataQuery.setMaxResults(size);

    List<EmployeeQueryResponse> employees = dataQuery.getResultList().stream()
        .map(employeeMapper::toEmployeeQueryResponse)
        .collect(Collectors.toList());

    return new PagedEmployeeResponse(
        employees,
        page,
        totalPages,
        page >= totalPages - 1
    );
  }
}