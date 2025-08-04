package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.RecognitionRequest;
import com.joycrew.backend.entity.enums.Tag;
import com.joycrew.backend.exception.GlobalExceptionHandler;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.RecognitionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecognitionController.class)
@Import(GlobalExceptionHandler.class)
class RecognitionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RecognitionService recognitionService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private EmployeeDetailsService employeeDetailsService;

    @Test
    @DisplayName("POST /api/recognitions - 포인트 선물 성공")
    @WithMockUserPrincipal // 모의 인증 객체 주입
    void sendPoints_Success() throws Exception {
        // Given
        RecognitionRequest request = new RecognitionRequest(2L, 100, "Great collaboration!", List.of(Tag.TEAMWORK));
        doNothing().when(recognitionService).sendRecognition(anyString(), any(RecognitionRequest.class));

        // When & Then
        mockMvc.perform(post("/api/recognitions")
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("포인트를 성공적으로 보냈습니다."));
    }

    @Test
    @DisplayName("POST /api/recognitions - 실패 (Request Body 유효성 검사 오류)")
    @WithMockUserPrincipal
    void sendPoints_Failure_InvalidRequest() throws Exception {
        // Given: 포인트(points)가 1 미만인 잘못된 요청
        RecognitionRequest invalidRequest = new RecognitionRequest(2L, 0, "Invalid points", List.of(Tag.GOALS));

        // When & Then
        mockMvc.perform(post("/api/recognitions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) // 400 Bad Request 예상
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/recognitions - 실패 (인증되지 않은 사용자)")
    void sendPoints_Failure_Unauthenticated() throws Exception {
        // Given
        RecognitionRequest request = new RecognitionRequest(2L, 100, "Great job!", List.of(Tag.CUSTOMERS));

        // When & Then
        mockMvc.perform(post("/api/recognitions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // 401 Unauthorized 예상
    }
}