package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.GiftPointRequest;
import com.joycrew.backend.entity.enums.Tag;
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
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GiftPointController.class)
class GiftPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GiftPointService giftPointService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUserPrincipal(email = "sender@example.com")
    @DisplayName("동료에게 포인트 선물 성공")
    void testGiftPointsSuccess() throws Exception {
        // given
        GiftPointRequest request = new GiftPointRequest(
                2L,
                50,
                "수고하셨어요!",
                List.of(Tag.TEAMWORK, Tag.INNOVATION)
        );

        // mock 서비스 로직
        doNothing().when(giftPointService).giftPointsToColleague(any(), any());

        // when & then
        mockMvc.perform(post("/api/gift-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("포인트를 성공적으로 보냈습니다."));
    }
}
