package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import com.joycrew.backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "사용자", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final EmployeeRepository employeeRepository;
    private final WalletRepository walletRepository;
    private final EmployeeService employeeService;

    @Operation(summary = "사용자 프로필 조회", description = "JWT 토큰으로 인증된 사용자의 프로필 정보를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("인증된 사용자를 찾을 수 없습니다."));

        Optional<Wallet> walletOptional = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId());
        int totalBalance = walletOptional.map(Wallet::getBalance).orElse(0);
        int giftableBalance = walletOptional.map(Wallet::getGiftablePoint).orElse(0);

        UserProfileResponse response = UserProfileResponse.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getEmployeeName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .position(employee.getPosition())
                .totalBalance(totalBalance)
                .giftableBalance(giftableBalance)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경 (첫 로그인 시)", description = "초기 비밀번호를 받은 사용자가 자신의 비밀번호를 새로 설정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (비밀번호 정책 위반)"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/password")
    public ResponseEntity<Map<String, String>> forceChangePassword(Authentication authentication, @Valid @RequestBody PasswordChangeRequest request) {
        String userEmail = authentication.getName();
        employeeService.forcePasswordChange(userEmail, request);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}
