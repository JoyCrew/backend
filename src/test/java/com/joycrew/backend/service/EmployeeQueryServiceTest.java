package com.joycrew.backend.service;

import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeQueryServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private EmployeeQueryService employeeQueryService;

    @Test
    @DisplayName("[Service] 직원 목록 조회 - 페이징 정보와 함께 반환")
    void getEmployees_Success() {
        // Given
        String keyword = "test";
        int page = 0;
        int size = 10;
        Long currentUserId = 1L;

        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        TypedQuery<Employee> dataQuery = mock(TypedQuery.class);
        Employee mockEmployee = Employee.builder().employeeId(2L).employeeName("Test User").build();

        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        when(em.createQuery(anyString(), eq(Employee.class))).thenReturn(dataQuery);
        when(dataQuery.setParameter(anyString(), any())).thenReturn(dataQuery);
        when(dataQuery.setFirstResult(anyInt())).thenReturn(dataQuery);
        when(dataQuery.setMaxResults(anyInt())).thenReturn(dataQuery);
        when(dataQuery.getResultList()).thenReturn(List.of(mockEmployee));

        // When
        PagedEmployeeResponse response = employeeQueryService.getEmployees(keyword, page, size, currentUserId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.employees()).hasSize(1);
        assertThat(response.currentPage()).isEqualTo(page);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.isLastPage()).isTrue();

        verify(dataQuery, times(1)).setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        verify(dataQuery, times(1)).setParameter("currentUserId", currentUserId);
    }
}