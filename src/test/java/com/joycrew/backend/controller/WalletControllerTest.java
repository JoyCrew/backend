package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private EmployeeDetailsService employeeDetailsService;

    @Test
    @DisplayName("GET /api/wallet/point - 포인트 잔액 조회 성공")
    @WithMockUserPrincipal
    void getWalletPoint_Success() throws Exception {
        // Given
        PointBalanceResponse mockResponse = new PointBalanceResponse(1500, 100);
        when(walletService.getPointBalance("testuser@joycrew.com")).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/wallet/point"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalBalance").value(1500))
                .andExpect(jsonPath("$.giftableBalance").value(100));
    }

    @Test
    @DisplayName("GET /api/wallet/point - 인증되지 않은 사용자 접근 시 401 반환")
    void getWalletPoint_Failure_Unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wallet/point"))
                .andExpect(status().isUnauthorized());
    }
}
