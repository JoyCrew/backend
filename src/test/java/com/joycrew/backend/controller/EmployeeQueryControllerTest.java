package com.joycrew.backend.controller;

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

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private EmployeeQueryService employeeQueryService;
  @MockBean
  private JwtUtil jwtUtil;
  @MockBean
  private EmployeeDetailsService employeeDetailsService;

  @Test
  @DisplayName("GET /api/employee/query - Should search employees successfully")
  @WithMockUserPrincipal
  void searchEmployees_success() throws Exception {
    // Given
    EmployeeQueryResponse mockEmployee = new EmployeeQueryResponse(
            2L, "https://cdn.joycrew.com/profile/user1.jpg",
            "Jane Doe", "HR", "Staff"
    );
    PagedEmployeeResponse mockResponse = new PagedEmployeeResponse(List.of(mockEmployee), 0, 1, true);
    when(employeeQueryService.getEmployees(anyString(), anyInt(), anyInt(), anyLong()))
            .thenReturn(mockResponse);

    // When & Then
    mockMvc.perform(get("/api/employee/query")
                    .param("keyword", "Jane")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.employees[0].employeeName").value("Jane Doe"))
            .andExpect(jsonPath("$.currentPage").value(0));
  }
}