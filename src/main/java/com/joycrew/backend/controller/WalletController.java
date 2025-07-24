package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
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

@Tag(name = "지갑", description = "포인트 관련 API")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletRepository walletRepository;

    @Operation(summary = "포인트 잔액 조회", description = "현재 로그인된 사용자의 포인트 잔액을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PointBalanceResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"로그인이 필요합니다.\"}")))
    })
    @GetMapping("/point")
    public ResponseEntity<?> getWalletPoint(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        String userEmail = authentication.getName();

        Long employeeId = ((Employee) authentication.getPrincipal()).getEmployeeId();

        Optional<Wallet> walletOptional = walletRepository.findByEmployee_EmployeeId(employeeId);

        int totalBalance = 0;
        int giftableBalance = 0;

        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            totalBalance = wallet.getBalance();
            giftableBalance = wallet.getGiftablePoint();
        }

        return ResponseEntity.ok(new PointBalanceResponse(totalBalance, giftableBalance));
    }
}