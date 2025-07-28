package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.EmployeeQueryType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeQueryService {

    @PersistenceContext
    private final EntityManager em;

    public List<EmployeeQueryResponse> getEmployees(EmployeeQueryType type, String keyword, int page, int size) {
        int offset = page * size;

        StringBuilder jpql = new StringBuilder("SELECT e FROM Employee e " +
                "JOIN FETCH e.company c " +
                "LEFT JOIN FETCH e.department d ");
        if (StringUtils.hasText(keyword)) {
            jpql.append("WHERE ");
            switch (type) {
                case EMAIL -> jpql.append("LOWER(e.email) LIKE :keyword ");
                case DEPARTMENT -> jpql.append("LOWER(d.name) LIKE :keyword ");
                default -> jpql.append("LOWER(e.employeeName) LIKE :keyword ");
            }
        }

        jpql.append("ORDER BY e.createdAt DESC");

        TypedQuery<Employee> query = em.createQuery(jpql.toString(), Employee.class);

        if (StringUtils.hasText(keyword)) {
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }

        query.setFirstResult(offset);
        query.setMaxResults(size);

        return query.getResultList().stream()
                .map(EmployeeQueryResponse::from)
                .toList();
    }
}
