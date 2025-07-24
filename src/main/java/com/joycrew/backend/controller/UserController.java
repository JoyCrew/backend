package com.joycrew.backend.controller;

import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.repository.WalletRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "사용자 프로필 조회", description = "JWT 토큰으로 인증된 사용자의 프로필 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"유효하지 않은 토큰입니다.\"}")))
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "유효하지 않은 토큰입니다."));
        }

        String userEmail = authentication.getName();

        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("인증된 사용자를 찾을 수 없습니다."));

        Optional<Wallet> walletOptional = walletRepository.findByEmployee_EmployeeId(employee.getEmployeeId());
        int totalBalance = 0;
        int giftableBalance = 0;
        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            totalBalance = wallet.getBalance();
            giftableBalance = wallet.getGiftablePoint();
        }

        UserProfileResponse response = UserProfileResponse.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getEmployeeName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null) // 부서 이름 추가
                .position(employee.getPosition())
                .totalBalance(totalBalance)
                .giftableBalance(giftableBalance)
                .build();

        return ResponseEntity.ok(response);
    }
}