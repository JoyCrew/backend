package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.dto.UserProfileUpdateRequest;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private EmployeeService employeeService;
  @MockBean
  private JwtUtil jwtUtil;
  @MockBean
  private EmployeeDetailsService employeeDetailsService;

  @Test
  @DisplayName("GET /api/user/profile - Should get profile successfully")
  @WithMockUserPrincipal
  void getProfile_Success() throws Exception {
    // Given
    UserProfileResponse mockResponse = new UserProfileResponse(
        1L, "Test User", "testuser@joycrew.com", null,
        1500, 100, AdminLevel.EMPLOYEE, "Engineering", "Staff",
        null, null, null
    );
    when(employeeService.getUserProfile("testuser@joycrew.com")).thenReturn(mockResponse);

    // When & Then
    mockMvc.perform(get("/api/user/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test User"));
  }

  @Test
  @DisplayName("POST /api/user/password - Should change password successfully")
  @WithMockUserPrincipal
  void forcePasswordChange_Success() throws Exception {
    // Given
    PasswordChangeRequest request = new PasswordChangeRequest("newPassword123!");
    doNothing().when(employeeService).forcePasswordChange(eq("testuser@joycrew.com"), any(PasswordChangeRequest.class));

    // When & Then
    mockMvc.perform(post("/api/user/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Password changed successfully."));
  }

  @Test
  @DisplayName("PATCH /api/user/profile - Should update profile successfully")
  @WithMockUserPrincipal
  void updateMyProfile_Success() throws Exception {
    // Given
    UserProfileUpdateRequest requestDto = new UserProfileUpdateRequest("New Name", null, null, null, null, "New Address");
    MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", objectMapper.writeValueAsBytes(requestDto));
    MockMultipartFile imagePart = new MockMultipartFile("profileImage", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image_bytes".getBytes());

    // When & Then
    mockMvc.perform(multipart("/api/user/profile")
            .file(requestPart)
            .file(imagePart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            })
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Your information has been updated successfully."));
  }
}