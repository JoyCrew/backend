package com.joycrew.backend.controller;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(UserControllerTest.TestControllerAdvice.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeRepository employeeRepository;
    @MockBean
    private WalletRepository walletRepository;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private EmployeeDetailsService employeeDetailsService;

    @ControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private Employee testEmployee;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .employeeId(1L)
                .email("testuser@joycrew.com")
                .employeeName("테스트유저")
                .role(UserRole.EMPLOYEE)
                .build();

        testWallet = Wallet.builder()
                .balance(1500)
                .giftablePoint(100)
                .build();
    }

    @Test
    @DisplayName("GET /api/user/profile - 프로필 조회 성공 (인증된 사용자)")
    @WithMockUser(username = "testuser@joycrew.com")
    void getProfile_Success_AuthenticatedUser() throws Exception {
        when(employeeRepository.findByEmail("testuser@joycrew.com")).thenReturn(Optional.of(testEmployee));
        when(walletRepository.findByEmployee_EmployeeId(1L)).thenReturn(Optional.of(testWallet));

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트유저"));
    }

    @Test
    @DisplayName("GET /api/user/profile - 프로필 조회 실패 (인증되지 않은 사용자)")
    void getProfile_Failure_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/user/profile - 프로필 조회 실패 (인증은 되었으나 사용자 정보 없음)")
    @WithMockUser(username = "nonexistent@joycrew.com")
    void getProfile_Failure_UserNotFoundAfterAuth() throws Exception {
        when(employeeRepository.findByEmail("nonexistent@joycrew.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("인증된 사용자를 찾을 수 없습니다."));
    }
}