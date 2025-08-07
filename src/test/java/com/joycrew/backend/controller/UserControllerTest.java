package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.dto.UserProfileUpdateRequest;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.exception.GlobalExceptionHandler;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EmployeeService employeeService;

    @Test
    @DisplayName("GET /api/user/profile - 프로필 조회 성공")
    @WithMockUserPrincipal
    void getProfile_Success() throws Exception {
        // Given
        UserProfileResponse mockResponse = new UserProfileResponse(
                1L, "테스트유저", "testuser@joycrew.com",
                "https://cdn.joycrew.com/profile/testuser.jpg",
                1500, 100, AdminLevel.EMPLOYEE, "개발팀", "사원",
                LocalDate.of(1995, 5, 10), // birthday
                "서울시 강남구",                  // address
                LocalDate.of(2023, 1, 1)    // hireDate
        );
        when(employeeService.getUserProfile("testuser@joycrew.com")).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.address").value("서울시 강남구"));
    }

    @Test
    @DisplayName("POST /api/user/password - 비밀번호 변경 성공")
    @WithMockUserPrincipal
    void forceChangePassword_Success() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest("newPassword123!");
        doNothing().when(employeeService).forcePasswordChange(eq("testuser@joycrew.com"), any(PasswordChangeRequest.class));

        // When & Then
        mockMvc.perform(post("/api/user/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));
    }

    @Test
    @DisplayName("PATCH /api/user/profile - 내 정보 수정 성공")
    @WithMockUserPrincipal
    void updateMyProfile_Success() throws Exception {
        // Given
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "새로운 내 이름", "http://new.image.url", null, null,
                LocalDate.of(2000, 1, 1), // birthday
                "경기도 성남시"                   // address
        );

        // When & Then
        mockMvc.perform(patch("/api/user/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 정보가 성공적으로 수정되었습니다."));
    }
}
