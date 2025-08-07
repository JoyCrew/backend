package com.joycrew.backend.service;

import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.service.mapper.EmployeeMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeQueryServiceTest {

    @Mock
    private EntityManager em;
    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeQueryService employeeQueryService;

    @Test
    @DisplayName("[Unit] Get employee list - Should return with paging information")
    void getEmployees_Success() {
        // Given
        String keyword = "test";
        int page = 0;
        int size = 10;
        Long currentUserId = 1L;

        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        TypedQuery<Employee> dataQuery = mock(TypedQuery.class);
        Employee mockEmployee = Employee.builder().employeeId(2L).employeeName("Test User").build();
        EmployeeQueryResponse mockDto = new EmployeeQueryResponse(2L, null, "Test User", "Test Dept", "Tester");

        // Mocking for the count query
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        // Mocking for the data query
        when(em.createQuery(anyString(), eq(Employee.class))).thenReturn(dataQuery);
        when(dataQuery.setParameter(anyString(), any())).thenReturn(dataQuery);
        when(dataQuery.setFirstResult(anyInt())).thenReturn(dataQuery);
        when(dataQuery.setMaxResults(anyInt())).thenReturn(dataQuery);
        when(dataQuery.getResultList()).thenReturn(List.of(mockEmployee));

        // Mocking the mapper's behavior
        when(employeeMapper.toEmployeeQueryResponse(any(Employee.class))).thenReturn(mockDto);

        // When
        PagedEmployeeResponse response = employeeQueryService.getEmployees(keyword, page, size, currentUserId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.employees()).hasSize(1);
        assertThat(response.employees().get(0).employeeName()).isEqualTo("Test User");
        assertThat(response.currentPage()).isEqualTo(page);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.isLastPage()).isTrue();

        verify(dataQuery, times(1)).setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        verify(dataQuery, times(1)).setParameter("currentUserId", currentUserId);
    }
}