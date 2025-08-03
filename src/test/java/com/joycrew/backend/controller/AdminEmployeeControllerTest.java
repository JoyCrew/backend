package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        // Given - 요청 DTO (회사명/부서명 기반)
        EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                "김여은",                      // name
                "kye02@example.com",          // email
                "password123!",               // initialPassword
                "조이크루",                   // companyName
                "인사팀",                     // departmentName
                "사원",                        // position
                UserRole.EMPLOYEE             // role
        );

        // Given - 서비스가 반환할 Employee mock 객체
        Employee mockEmployee = Employee.builder()
                .employeeId(1L)
                .employeeName("김여은")
                .email("kye02@example.com")
                .company(Company.builder().companyId(1L).companyName("조이크루").build())
                .department(Department.builder().departmentId(1L).name("인사팀").build())
                .position("사원")
                .role(UserRole.EMPLOYEE)
                .build();

        when(adminEmployeeService.registerEmployee(any(EmployeeRegistrationRequest.class)))
                .thenReturn(mockEmployee);

        // When & Then - 요청 수행 및 응답 검증
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

}
