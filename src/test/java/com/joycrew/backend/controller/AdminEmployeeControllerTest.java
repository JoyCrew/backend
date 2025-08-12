package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.AdminEmployeeUpdateRequest;
import com.joycrew.backend.dto.AdminPointDistributionRequest;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.dto.PointDistributionDetail;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.entity.enums.TransactionType;
import com.joycrew.backend.security.WithMockUserPrincipal;
import com.joycrew.backend.service.AdminPointService;
import com.joycrew.backend.service.EmployeeManagementService;
import com.joycrew.backend.service.EmployeeRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminEmployeeController.class)
class AdminEmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EmployeeRegistrationService registrationService;
    @MockBean private EmployeeManagementService managementService;
    @MockBean private AdminPointService pointService;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("POST /api/admin/employees - Should register employee successfully")
    void registerEmployee_success() throws Exception {
        // Given
        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                "Jane Doe", "jane.doe@example.com", "password123!",
                "JoyCrew", "HR", "Staff", AdminLevel.EMPLOYEE,
                null, null, null
        );
        Employee mockEmployee = Employee.builder().employeeId(1L).build();
        when(registrationService.registerEmployee(any(EmployeeRegistrationRequest.class)))
                .thenReturn(mockEmployee);

        // When & Then
        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee created successfully (ID: 1)"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    @DisplayName("POST /api/admin/employees/bulk - Should bulk register employees successfully")
    void registerEmployeesFromCsv_success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "employees.csv", "text/csv",
                "name,email,initialPassword,companyName,departmentName,position,role\n".getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        mockMvc.perform(multipart("/api/admin/employees/bulk")
                        .file(file)
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("CSV processed and employee registration completed."));
    }

    @Test
    @DisplayName("PATCH /api/admin/employees/{id} - Should update employee successfully")
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateEmployee_Success() throws Exception {
        // Given
        AdminEmployeeUpdateRequest request = new AdminEmployeeUpdateRequest("Updated Name", null, "Manager", null, null);

        // When & Then
        mockMvc.perform(patch("/api/admin/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee information updated successfully."));
    }

    @Test
    @DisplayName("DELETE /api/admin/employees/{id} - Should deactivate employee successfully")
    @WithMockUser(roles = "SUPER_ADMIN")
    void deleteEmployee_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/admin/employees/1")
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee successfully deactivated."));
    }

    @Test
    @DisplayName("POST /api/admin/points/distribute - Should distribute points successfully")
    @WithMockUserPrincipal(role="SUPER_ADMIN")
    void distributePoints_Success() throws Exception {
        // Given
        List<PointDistributionDetail> distributions = List.of(
                new PointDistributionDetail(1L, 100),
                new PointDistributionDetail(2L, 100)
        );

        AdminPointDistributionRequest request = new AdminPointDistributionRequest(
                distributions,
                "Bonus",
                TransactionType.AWARD_MANAGER_SPOT
        );
        // When & Then
        mockMvc.perform(post("/api/admin/employees/points/distribute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Point distribution process completed successfully."));
    }
}