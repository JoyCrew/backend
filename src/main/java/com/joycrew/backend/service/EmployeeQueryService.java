package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AccessStatus;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

  @PersistenceContext
  private final EntityManager em;
  private final EmployeeMapper employeeMapper;

  public PagedEmployeeResponse getEmployees(String keyword, int page, int size, Long currentUserId, AccessStatus accessStatus) {
    StringBuilder whereClause = new StringBuilder();
    boolean hasKeyword = StringUtils.hasText(keyword);

    if (hasKeyword) {
      whereClause.append("WHERE (LOWER(e.employeeName) LIKE :keyword ")
          .append("OR LOWER(e.email) LIKE :keyword ")
          .append("OR LOWER(d.name) LIKE :keyword) ");
    }

    whereClause.append(hasKeyword ? "AND " : "WHERE ");
    whereClause.append("e.id != :currentUserId ");

    String countJpql = "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d " + whereClause;
    TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
    countQuery.setParameter("currentUserId", currentUserId);
    if (hasKeyword) {
      countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    }
    long totalCount = countQuery.getSingleResult();
    int totalPages = (int) Math.ceil((double) totalCount / size);

    String dataJpql = "SELECT e FROM Employee e " +
        "JOIN FETCH e.company c " +
        "LEFT JOIN FETCH e.department d " +
        whereClause +
        "ORDER BY e.employeeName ASC";
    TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class);
    dataQuery.setParameter("currentUserId", currentUserId);
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