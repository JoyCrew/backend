package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.exception.GlobalExceptionHandler;
import com.joycrew.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance(); // 테스트용
        }
    }

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("test@joycrew.com", "password123!");
        LoginResponse successResponse = new LoginResponse(
                "mocked.jwt.token", "로그인 성공", 1L, "테스트유저", "test@joycrew.com", UserRole.EMPLOYEE
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.message").value("로그인 성공"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 로그인 실패 (잘못된 비밀번호)")
    void login_Failure_WrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@joycrew.com", "wrongpassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 로그인 실패 (이메일 없음)")
    void login_Failure_EmailNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@joycrew.com", "anypassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - 로그아웃 성공")
    void logout_Success() throws Exception {
        doNothing().when(authService).logout(any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer some.mock.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));
    }
}
