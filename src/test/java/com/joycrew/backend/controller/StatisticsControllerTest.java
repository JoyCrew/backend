package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PointStatisticsResponse;
import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StatisticsService statisticsService;

  @MockBean
  private JwtUtil jwtUtil;
  @MockBean
  private EmployeeDetailsService employeeDetailsService;

  @Test
  @DisplayName("GET /api/statistics/me - Should return detailed statistics successfully")
  @WithMockUserPrincipal
  void getMyStatistics_Success() throws Exception {
    // Given
    List<Long> mockTagCounts = List.of(3L, 5L, 6L, 0L, 0L, 0L, 0L, 0L);
    List<TransactionHistoryResponse> mockRecentTransactions = Collections.emptyList();
    PointStatisticsResponse mockResponse = new PointStatisticsResponse(
        100, 50, mockTagCounts, mockRecentTransactions
    );

    when(statisticsService.getPointStatistics(anyString())).thenReturn(mockResponse);

    // When & Then
    mockMvc.perform(get("/api/statistics/me"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.totalPointsReceived").value(100))
        .andExpect(jsonPath("$.totalPointsSent").value(50))
        .andExpect(jsonPath("$.tagCounts[0]").value(3))
        .andExpect(jsonPath("$.tagCounts[1]").value(5))
        .andExpect(jsonPath("$.recentTransactions").isArray());
  }
}