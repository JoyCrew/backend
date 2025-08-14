package com.joycrew.backend.controller;

import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.TransactionHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionHistoryController.class)
class TransactionHistoryControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private TransactionHistoryService transactionHistoryService;
  @MockBean
  private JwtUtil jwtUtil;
  @MockBean
  private EmployeeDetailsService employeeDetailsService;

  @Test
  @DisplayName("GET /api/transactions - Should get my transaction history successfully")
  @WithMockUserPrincipal(email = "user@joycrew.com")
  void getMyTransactions_Success() throws Exception {
    // Given
    List<TransactionHistoryResponse> mockHistory = List.of(
            TransactionHistoryResponse.builder()
                    .transactionId(1L).type(TransactionType.AWARD_P2P).amount(-50)
                    .counterparty("Colleague Name").message("Thanks!")
                    .transactionDate(LocalDateTime.now()).build()
    );
    when(transactionHistoryService.getTransactionHistory("user@joycrew.com")).thenReturn(mockHistory);

    // When & Then
    mockMvc.perform(get("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].transactionId").value(1L))
            .andExpect(jsonPath("$[0].counterparty").value("Colleague Name"));
  }
}