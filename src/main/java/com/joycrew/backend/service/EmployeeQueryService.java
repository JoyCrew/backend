package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.service.mapper.EmployeeMapper;
import com.joycrew.backend.tenant.Tenant;
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

  public PagedEmployeeResponse getEmployees(String keyword, int page, int size, Long currentUserId, AdminLevel requesterRole) {
    Long tenant = Tenant.id(); // ✅ 테넌트

    StringBuilder where = new StringBuilder("WHERE c.companyId = :tenant AND e.employeeId != :currentUserId ");

    boolean hasKeyword = StringUtils.hasText(keyword);
    if (hasKeyword) {
      where.append("AND (LOWER(e.employeeName) LIKE :keyword ")
              .append("OR LOWER(e.email) LIKE :keyword ")
              .append("OR LOWER(d.name) LIKE :keyword) ");
    }

    boolean hideSuperAdmin = (requesterRole != AdminLevel.SUPER_ADMIN);
    if (hideSuperAdmin) {
      where.append("AND e.role <> :superAdmin ");
    }

    String countJpql = "SELECT COUNT(e) FROM Employee e JOIN e.company c LEFT JOIN e.department d " + where;
    TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class)
            .setParameter("tenant", tenant)
            .setParameter("currentUserId", currentUserId);
    if (hasKeyword) countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    if (hideSuperAdmin) countQuery.setParameter("superAdmin", AdminLevel.SUPER_ADMIN);

    long totalCount = countQuery.getSingleResult();
    int totalPages = (int) Math.ceil((double) totalCount / size);

    String dataJpql =
            "SELECT e FROM Employee e " +
                    "JOIN FETCH e.company c " +
                    "LEFT JOIN FETCH e.department d " +
                    where +
                    "ORDER BY e.employeeName ASC";

    TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class)
            .setParameter("tenant", tenant)
            .setParameter("currentUserId", currentUserId);
    if (hasKeyword) dataQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
    if (hideSuperAdmin) dataQuery.setParameter("superAdmin", AdminLevel.SUPER_ADMIN);

    dataQuery.setFirstResult(page * size);
    dataQuery.setMaxResults(size);

    List<EmployeeQueryResponse> employees = dataQuery.getResultList().stream()
            .filter(e -> !(hideSuperAdmin && e.getRole() == AdminLevel.SUPER_ADMIN)) // 최종 안전망
            .map(employeeMapper::toEmployeeQueryResponse)
            .collect(Collectors.toList());

    return new PagedEmployeeResponse(employees, page, totalPages, page >= totalPages - 1);
  }
}
