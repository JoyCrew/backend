package com.joycrew.backend.controller;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletRepository walletRepository;

    @Autowired
    private WebApplicationContext context;

    private Employee testEmployee;
    private Wallet testWallet;
    private Employee noWalletEmployee;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testEmployee = Employee.builder()
                .employeeId(1L)
                .email("testuser@joycrew.com")
                .employeeName("테스트유저")
                .role(UserRole.EMPLOYEE)
                .status("ACTIVE")
                .passwordHash("{noop}password")
                .build();

        testWallet = Wallet.builder()
                .walletId(100L)
                .employee(testEmployee)
                .balance(1500)
                .giftablePoint(100)
                .build();

        noWalletEmployee = Employee.builder()
                .employeeId(99L)
                .email("nowallet@joycrew.com")
                .passwordHash("{noop}password")
                .employeeName("지갑없음")
                .role(UserRole.EMPLOYEE)
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("GET /api/wallet/point - 포인트 잔액 조회 성공 (인증된 사용자)")
    @WithUserDetails(value = "testuser@joycrew.com", userDetailsServiceBeanName = "testUserDetailsService")
    void getWalletPoint_Success_WithValidToken() throws Exception {
        when(walletRepository.findByEmployee_EmployeeId(testEmployee.getEmployeeId()))
                .thenReturn(Optional.of(testWallet));

        mockMvc.perform(get("/api/wallet/point")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalBalance").value(testWallet.getBalance()))
                .andExpect(jsonPath("$.giftableBalance").value(testWallet.getGiftablePoint()));
    }

    @Test
    @DisplayName("GET /api/wallet/point - 포인트 잔액 조회 실패 (인증되지 않은 사용자)")
    void getWalletPoint_Failure_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/wallet/point")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("GET /api/wallet/point - 포인트 잔액 조회 성공 (인증은 되었으나 지갑 없음)")
    @WithUserDetails(value = "nowallet@joycrew.com", userDetailsServiceBeanName = "testUserDetailsService")
    void getWalletPoint_Success_WalletNotFound() throws Exception {
        when(walletRepository.findByEmployee_EmployeeId(noWalletEmployee.getEmployeeId()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/wallet/point")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalBalance").value(0))
                .andExpect(jsonPath("$.giftableBalance").value(0));
    }
}