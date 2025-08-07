package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.EmployeeQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeQueryController.class)
class EmployeeQueryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EmployeeQueryService employeeQueryService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private EmployeeDetailsService employeeDetailsService;

    @Test
    @DisplayName("GET /api/employee/query - 직원 목록 검색 성공")
    @WithMockUserPrincipal
    void searchEmployees_success() throws Exception {
        EmployeeQueryResponse mockEmployee = new EmployeeQueryResponse(
                2L,
                "https://cdn.joycrew.com/profile/user1.jpg",
                "김여은",
                "인사팀",
                "사원"
        );
        PagedEmployeeResponse mockResponse = new PagedEmployeeResponse(List.of(mockEmployee), 0, 1, true);

        when(employeeQueryService.getEmployees(anyString(), anyInt(), anyInt(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/employee/query")
                        .param("keyword", "김")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].employeeName").value("김여은"))
                .andExpect(jsonPath("$.employees[0].departmentName").value("인사팀"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.isLastPage").value(true));
    }

    @Test
    @DisplayName("GET /api/employee/query - 검색어 없이도 정상 조회")
    @WithMockUserPrincipal
    void searchEmployees_noKeyword() throws Exception {
        EmployeeQueryResponse mockEmployee = new EmployeeQueryResponse(
                2L,
                null,
                "홍길동",
                null,
                "주임"
        );
        PagedEmployeeResponse mockResponse = new PagedEmployeeResponse(List.of(mockEmployee), 0, 1, true);

        when(employeeQueryService.getEmployees(isNull(), anyInt(), anyInt(), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/employee/query")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].employeeName").value("홍길동"))
                .andExpect(jsonPath("$.employees[0].position").value("주임"));
    }
}
