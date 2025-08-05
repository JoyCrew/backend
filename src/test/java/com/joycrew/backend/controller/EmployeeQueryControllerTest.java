package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PagedEmployeeResponse;
import com.joycrew.backend.service.EmployeeQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "testuser", roles = {"EMPLOYEE"})
@WebMvcTest(controllers = EmployeeQueryController.class)
class EmployeeQueryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EmployeeQueryService employeeQueryService;

    @Test
    @DisplayName("GET /api/employee/query - 직원 목록 검색 성공")
    void searchEmployees_success() throws Exception {
        // Given
        EmployeeQueryResponse mockEmployee = new EmployeeQueryResponse(
                1L,
                "https://cdn.joycrew.com/profile/user1.jpg",
                "김여은",
                "인사팀",
                "사원"
        );


        when(employeeQueryService.getEmployees(anyString(), anyInt(), anyInt(), anyLong()))
                .thenReturn(new PagedEmployeeResponse(
                        List.of(mockEmployee),
                        0, 1, true
                ));

        // When & Then
        mockMvc.perform(get("/api/employee/query")
                        .param("keyword", "김")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("김여은"))
                .andExpect(jsonPath("$[0].departmentName").value("인사팀"))
                .andExpect(jsonPath("$[0].position").value("사원"))
                .andExpect(jsonPath("$[0].profileImageUrl").value("https://cdn.joycrew.com/profile/user1.jpg"));
    }

    @Test
    @DisplayName("GET /api/employee/query - 검색어 없이도 정상 조회")
    void searchEmployees_noKeyword() throws Exception {
        // Given
        EmployeeQueryResponse mockEmployee = new EmployeeQueryResponse(
                2L,
                null,
                "홍길동",
                null,
                "주임"
        );


        when(employeeQueryService.getEmployees(isNull(), anyInt(), anyInt(), anyLong()))
                .thenReturn(new PagedEmployeeResponse(
                        List.of(mockEmployee),
                        0, 1, true
                ));

        // When & Then
        mockMvc.perform(get("/api/employee/query")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("홍길동"))
                .andExpect(jsonPath("$[0].position").value("주임"));
    }

    @Test
    void debug_print_response() throws Exception {
        mockMvc.perform(get("/api/employee/query")
                        .param("keyword", "김")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print()) // 👈 응답을 콘솔에 출력
                .andExpect(status().isOk());
    }

}
