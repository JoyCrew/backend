package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.dto.PasswordResetConfirmRequest;
import com.joycrew.backend.dto.PasswordResetRequest;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.exception.GlobalExceptionHandler;
import com.joycrew.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - Should succeed with correct credentials")
    void login_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@joycrew.com", "password123!");
        LoginResponse successResponse = new LoginResponse(
                "mocked.jwt.token", "Login successful", 1L,
                "Test User", "test@joycrew.com", AdminLevel.EMPLOYEE,
                1000, "http://example.com/profile.jpg"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should fail with bad credentials")
    void login_Failure_AuthenticationError() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@joycrew.com", "wrongpassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Should succeed")
    void logout_Success() throws Exception {
        // Given
        doNothing().when(authService).logout(any(HttpServletRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You have been logged out."));
    }

    @Test
    @DisplayName("POST /api/auth/password-reset/request - Should succeed")
    void requestPasswordReset_Success() throws Exception {
        // Given
        PasswordResetRequest request = new PasswordResetRequest("user@example.com");
        doNothing().when(authService).requestPasswordReset(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("A password reset email has been requested. Please check your email."));
    }

    @Test
    @DisplayName("POST /api/auth/password-reset/confirm - Should succeed")
    void confirmPasswordReset_Success() throws Exception {
        // Given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("valid-token", "newPassword123!");
        doNothing().when(authService).confirmPasswordReset(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully."));
    }
}