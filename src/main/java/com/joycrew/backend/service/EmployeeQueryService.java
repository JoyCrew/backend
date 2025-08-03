package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

    @PersistenceContext
    private final EntityManager em;

    public PagedEmployeeResponse getEmployees(String keyword, int page, int size, Long currentUserId) {
        // WHERE 절과 파라미터 구성을 위한 기본 StringBuilder
        StringBuilder whereClause = new StringBuilder();
        boolean hasKeyword = StringUtils.hasText(keyword);

        if (hasKeyword) {
            whereClause.append("WHERE (LOWER(e.employeeName) LIKE :keyword ")
                    .append("OR LOWER(e.email) LIKE :keyword ")
                    .append("OR LOWER(d.name) LIKE :keyword) ");
        }

        // 본인 제외 조건 추가
        whereClause.append(hasKeyword ? "AND " : "WHERE ");
        whereClause.append("e.id != :currentUserId ");

        // 1. 전체 카운트를 가져오는 쿼리 실행
        String countJpql = "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d " + whereClause;
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        countQuery.setParameter("currentUserId", currentUserId);
        if (hasKeyword) {
            countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        long totalCount = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 2. 실제 데이터를 가져오는 쿼리 실행
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
                .map(EmployeeQueryResponse::from)
                .toList();

        // 3. PagedEmployeeResponse 로 감싸서 반환
        return new PagedEmployeeResponse(
                employees,
                page,
                totalPages,
                page >= totalPages - 1
        );
    }
}