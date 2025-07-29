package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.entity.enums.EmployeeQueryType;
import com.joycrew.backend.entity.enums.UserRole;
import com.joycrew.backend.service.EmployeeQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeQueryController.class)
class EmployeeQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeQueryService employeeQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("이름 기준 검색 - 성공")
    void searchByName_success() throws Exception {
        EmployeeQueryResponse mockResponse = EmployeeQueryResponse.builder()
                .employeeId(1L)
                .employeeName("김여은")
                .email("kye02@example.com")
                .position("사원")
                .status("ACTIVE")
                .role(UserRole.EMPLOYEE)
                .departmentName("인사팀")
                .companyName("조이크루")
                .build();

        when(employeeQueryService.getEmployees(
                any(EmployeeQueryType.class),
                any(String.class),
                any(Integer.class),
                any(Integer.class)
        )).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/employees")
                        .param("type", "NAME")
                        .param("keyword", "김")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("김여은"))
                .andExpect(jsonPath("$[0].email").value("kye02@example.com"))
                .andExpect(jsonPath("$[0].departmentName").value("인사팀"))
                .andDo(result -> {
                    // 실제 응답 구조 확인용 로그
                    String content = result.getResponse().getContentAsString();
                    System.out.println(">>> 응답 JSON: " + content);
                });
    }


    @Test
    @WithMockUser
    @DisplayName("키워드 없이 전체 조회")
    void searchWithoutKeyword_success() throws Exception {
        when(employeeQueryService.getEmployees(
                any(EmployeeQueryType.class),
                any(String.class),
                any(Integer.class),
                any(Integer.class)
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/employees")
                        .param("type", "NAME")
                        .param("keyword", "") // 👈 명시적으로 keyword 전달
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    System.out.println(">>> 전체 조회 응답 JSON: " + content);
                });
    }

}
