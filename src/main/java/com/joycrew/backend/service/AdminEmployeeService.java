package com.joycrew.backend.service;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.entity.*;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.exception.UserNotFoundException;
import com.joycrew.backend.repository.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final RewardPointTransactionRepository transactionRepository;
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
                .birthday(request.birthday())
                .address(request.address())
                .hireDate(request.hireDate())
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
                    AdminLevel adminLevel = parseAdminLevel(tokens[6].trim());
                    LocalDate birthday = parseDate(tokens[7].trim());
                    String address = tokens[8].trim();
                    LocalDate hireDate = parseDate(tokens[9].trim());

                    EmployeeRegistrationRequest request = new EmployeeRegistrationRequest(
                            tokens[0].trim(), // name
                            tokens[1].trim(), // email
                            tokens[2].trim(), // initialPassword
                            tokens[3].trim(), // companyName
                            tokens[4].trim().isBlank() ? null : tokens[4].trim(), // departmentName
                            tokens[5].trim(), // position
                            adminLevel,       // level
                            birthday,         // birthday
                            address,          // address
                            hireDate          // hireDate
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

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr); // YYYY-MM-DD 형식
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식: {}. null로 처리합니다.", dateStr);
            return null;
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

        String countJpql = "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d " + whereClause;
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        if (keyword != null && !keyword.isBlank()) {
            countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        long total = countQuery.getSingleResult();
        int totalPages = (int) Math.ceil((double) total / size);

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

    public Employee updateEmployee(Long employeeId, AdminEmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("ID가 " + employeeId + "인 직원을 찾을 수 없습니다."));

        if (request.name() != null) {
            employee.updateName(request.name());
        }
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException("ID가 " + request.departmentId() + "인 부서를 찾을 수 없습니다."));
            employee.assignToDepartment(department);
        }
        if (request.position() != null) {
            employee.updatePosition(request.position());
        }
        if (request.level() != null) {
            employee.updateRole(request.level());
        }
        if (request.status() != null) {
            employee.updateStatus(request.status());
        }
        return employeeRepository.save(employee);
    }

    public void disableEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("ID가 " + employeeId + "인 직원을 찾을 수 없습니다."));
        employee.updateStatus("DELETED");
        employeeRepository.save(employee);
    }

    public void distributePoints(AdminPointDistributionRequest request, Employee admin) {
        List<Employee> employees = employeeRepository.findAllById(request.employeeIds());
        if (employees.size() != request.employeeIds().size()) {
            throw new UserNotFoundException("일부 직원을 찾을 수 없습니다. 요청을 확인해주세요.");
        }

        for (Employee employee : employees) {
            Wallet wallet = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                    .orElseThrow(() -> new IllegalStateException(employee.getEmployeeName() + "님의 지갑이 없습니다."));

            if (request.points() > 0) {
                wallet.addPoints(request.points());
            } else {
                wallet.spendPoints(Math.abs(request.points()));
            }

            RewardPointTransaction transaction = RewardPointTransaction.builder()
                    .sender(admin)
                    .receiver(employee)
                    .pointAmount(request.points())
                    .message(request.message())
                    .type(request.type())
                    .build();
            transactionRepository.save(transaction);
        }
    }

    @Transactional(readOnly = true)
    public List<AdminEmployeeQueryResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(AdminEmployeeQueryResponse::from)
                .toList();
    }
}
