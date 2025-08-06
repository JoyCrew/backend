package com.joycrew.backend.service;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.entity.Company;
import com.joycrew.backend.entity.Department;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.repository.CompanyRepository;
import com.joycrew.backend.repository.DepartmentRepository;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private final EntityManager em;

    public Employee registerEmployee(EmployeeRegistrationRequest request) {
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        Company company = companyRepository.findByCompanyName(request.companyName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회사명입니다."));

        Department department = null;
        if (request.departmentName() != null && !request.departmentName().isBlank()) {
            department = departmentRepository.findByCompanyAndName(company, request.departmentName())
                    .orElseThrow(() -> new IllegalArgumentException("해당 회사에 존재하지 않는 부서명입니다."));
        }

        Employee newEmployee = Employee.builder()
                .employeeName(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.initialPassword()))
                .company(company)
                .department(department)
                .position(request.position())
                .role(request.level())
                .status("ACTIVE")
                .build();

        Employee savedEmployee = employeeRepository.save(newEmployee);
        walletRepository.save(new Wallet(savedEmployee));
        return savedEmployee;
    }

    public void registerEmployeesFromCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length < 6) {
                    log.warn("누락된 필드가 있는 행 건너뜀: {}", line);
                    continue;
                }

                try {
                    AdminLevel adminLevel = parseAdminLevel(tokens.length > 6 ? tokens[6].trim() : "EMPLOYEE");

                    EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                            tokens[0].trim(),
                            tokens[1].trim(),
                            tokens[2].trim(),
                            tokens[3].trim(),
                            tokens[4].trim().isBlank() ? null : tokens[4].trim(),
                            tokens[5].trim(),
                            adminLevel
                    );
                    registerEmployee(request);
                } catch (Exception e) {
                    log.warn("직원 등록 실패 - 입력값: [{}], 사유: {}", line, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 읽기 실패", e);
        }
    }

    private AdminLevel parseAdminLevel(String level) {
        if (level == null || level.isBlank()) {
            return AdminLevel.EMPLOYEE;
        }

        try {
            return AdminLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 권한 레벨: {}. 기본값 EMPLOYEE로 설정합니다.", level);
            return AdminLevel.EMPLOYEE;
        }
    }

    @Transactional(readOnly = true)
    public AdminPagedEmployeeResponse searchEmployees(String keyword, int page, int size) {
        StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            whereClause.append("AND (LOWER(e.employeeName) LIKE :keyword ")
                    .append("OR LOWER(e.email) LIKE :keyword ")
                    .append("OR LOWER(d.name) LIKE :keyword) ");
        }

        // 총 개수 조회
        String countJpql = "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d " + whereClause;
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        if (keyword != null && !keyword.isBlank()) {
            countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        long total = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) total / size);

        // 데이터 조회
        String dataJpql = "SELECT e FROM Employee e " +
                "LEFT JOIN FETCH e.department d " +
                "LEFT JOIN FETCH e.company c " +
                whereClause +
                "ORDER BY e.employeeName ASC";
        TypedQuery<Employee> dataQuery = em.createQuery(dataJpql, Employee.class)
                .setFirstResult(page * size)
                .setMaxResults(size);
        if (keyword != null && !keyword.isBlank()) {
            dataQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }

        // Admin 전용 DTO로 변환
        List<AdminEmployeeQueryResponse> employees = dataQuery.getResultList().stream()
                .map(AdminEmployeeQueryResponse::from)
                .toList();

        return new AdminPagedEmployeeResponse(
                employees,
                page + 1,
                totalPages,
                page >= totalPages - 1
        );
    }
}
