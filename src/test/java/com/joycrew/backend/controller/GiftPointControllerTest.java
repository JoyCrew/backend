package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.GiftPointRequest;
import com.joycrew.backend.entity.enums.Tag;
import com.joycrew.backend.security.EmployeeDetailsService;
import com.joycrew.backend.security.JwtUtil;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.GiftPointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GiftPointController.class)
class GiftPointControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private GiftPointService giftPointService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private EmployeeDetailsService employeeDetailsService;

    @Test
    @WithMockUserPrincipal(email = "sender@example.com")
    @DisplayName("POST /api/gift-points - Should gift points to a colleague successfully")
    void testGiftPointsSuccess() throws Exception {
        // given
        GiftPointRequest request = new GiftPointRequest(
                2L, 50, "Great work!", List.of(Tag.TEAMWORK)
        );
        doNothing().when(giftPointService).giftPointsToColleague(anyString(), any(GiftPointRequest.class));

        // when & then
        mockMvc.perform(post("/api/gift-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Points sent successfully."));
    }
}