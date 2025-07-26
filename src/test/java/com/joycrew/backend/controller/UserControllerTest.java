package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(UserControllerTest.TestControllerAdvice.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeRepository employeeRepository;
    @MockBean
    private WalletRepository walletRepository;
    @MockBean
    private EmployeeService employeeService;

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
    @DisplayName("POST /api/user/password - 비밀번호 변경 성공")
    @WithMockUser(username = "testuser@joycrew.com")
    void forceChangePassword_Success() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setNewPassword("newPassword123!");
        doNothing().when(employeeService).forcePasswordChange(eq("testuser@joycrew.com"), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(post("/api/user/password")
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));
    }

    @Test
    @DisplayName("POST /api/user/password - 비밀번호 변경 실패 (유효성 검사 실패)")
    @WithMockUser(username = "testuser@joycrew.com")
    void forceChangePassword_Failure_InvalidPassword() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setNewPassword("short");

        // When & Then
        mockMvc.perform(post("/api/user/password")
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}