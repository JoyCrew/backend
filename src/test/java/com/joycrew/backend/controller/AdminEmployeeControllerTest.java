package com.joycrew.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joycrew.backend.dto.AdminEmployeeQueryResponse;
import com.joycrew.backend.dto.AdminPagedEmployeeResponse;
import com.joycrew.backend.dto.EmployeeRegistrationRequest;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
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
                AdminLevel.EMPLOYEE
        );

        Employee mockEmployee = Employee.builder()
                .employeeId(1L)
                .employeeName("김여은")
                .email("kye02@example.com")
                .company(Company.builder().companyId(1L).companyName("조이크루").build())
                .department(Department.builder().departmentId(1L).name("인사팀").build())
                .position("사원")
                .role(AdminLevel.EMPLOYEE)
                .build();

        when(adminEmployeeService.registerEmployee(any(EmployeeRegistrationRequest.class)))
                .thenReturn(mockEmployee);

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

        mockMvc.perform(multipart("/api/admin/employees/bulk")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("CSV 업로드 및 직원 등록이 완료되었습니다."));
    }

    @Test
    @WithMockUser(roles = "HR_ADMIN")
    @DisplayName("GET /api/admin/employees - 전체 직원 목록 조회 (검색 포함)")
    void searchEmployees_success() throws Exception {
        AdminEmployeeQueryResponse employeeDto = new AdminEmployeeQueryResponse(
                1L,
                "김여은",
                "kye02@example.com",
                "조이크루",
                "인사팀",
                "사원",
                "https://cdn.joycrew.com/profile/1.jpg"
        );


        AdminPagedEmployeeResponse pagedResponse = new AdminPagedEmployeeResponse(
                List.of(employeeDto),
                1,
                1,
                true
        );

        when(adminEmployeeService.searchEmployees(any(), any(), any()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/admin/employees")
                        .param("keyword", "김여은")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.employees[0].employeeName").value("김여은"))
                .andExpect(jsonPath("$.employees[0].email").value("kye02@example.com"))
                .andExpect(jsonPath("$.employees[0].adminLevel").value("EMPLOYEE"));
    }
}
