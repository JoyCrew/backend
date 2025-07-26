package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지갑", description = "포인트 관련 API")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "포인트 잔액 조회", security = @SecurityRequirement(name = "Authorization"))
    @GetMapping("/point")
    public ResponseEntity<PointBalanceResponse> getWalletPoint(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(walletService.getPointBalance(principal.getUsername()));
    }
}