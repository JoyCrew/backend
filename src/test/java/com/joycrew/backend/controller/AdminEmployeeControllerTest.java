package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.AdminPointDistributionRequest;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.AdminEmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminEmployeeController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class AdminEmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminEmployeeService adminEmployeeService;

    @Test
    @WithMockUser(roles = "HR_ADMIN")
    @DisplayName("POST /api/admin/employees - 직원 등록 성공")
    void registerEmployee_success() throws Exception {
        // Given
        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                "김여은",
                "kye02@example.com",
                "password123!",
                "조이크루",
                "인사팀",
                "사원",
                AdminLevel.EMPLOYEE,
                null,
                null,
                null
        );

        Employee mockEmployee = Employee.builder().employeeId(1L).build();
        when(adminEmployeeService.registerEmployee(any(EmployeeRegistrationRequest.class)))
                .thenReturn(mockEmployee);

        // When & Then
        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("직원 생성 완료 (ID: 1)"));
    }

    @Test
    @WithMockUser(roles = "HR_ADMIN")
    @DisplayName("POST /api/admin/employees/bulk - 직원 일괄 등록 성공")
    void registerEmployeesFromCsv_success() throws Exception {
        // Given: 예제 CSV 내용
        String csvContent = """
            name,email,initialPassword,companyName,departmentName,position,role
            김여은,kye02@example.com,password123,조이크루,인사팀,사원,EMPLOYEE
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/employees/bulk")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("CSV 업로드 및 직원 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("PATCH /api/admin/employees/{id} - 직원 정보 업데이트 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateEmployee_Success() throws Exception {
        // Given
        AdminEmployeeUpdateRequest request = new AdminEmployeeUpdateRequest("업데이트된 이름", null, "팀장", null, null);

        // When & Then
        mockMvc.perform(patch("/api/admin/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("직원 정보가 성공적으로 업데이트되었습니다."));
    }

    @Test
    @DisplayName("DELETE /api/admin/employees/{id} - 직원 삭제 성공")
    @WithMockUser(roles = "SUPER_ADMIN")
    void deleteEmployee_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/admin/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("직원이 성공적으로 삭제(비활성화) 처리되었습니다."));
    }

    @Test
    @DisplayName("POST /api/admin/points/distribute - 포인트 분배 성공")
    @WithMockUserPrincipal(role="SUPER_ADMIN")
    void distributePoints_Success() throws Exception {
        // Given
        AdminPointDistributionRequest request = new AdminPointDistributionRequest(
                List.of(1L, 2L), 100, "보너스", TransactionType.ADMIN_ADJUSTMENT);

        // When & Then
        mockMvc.perform(post("/api/admin/employees/points/distribute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("포인트 분배(회수) 작업이 완료되었습니다."));
    }

}
