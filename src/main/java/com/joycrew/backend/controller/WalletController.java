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

@Tag(name = "Wallet", description = "APIs related to points and wallet")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Get point balance", security = @SecurityRequirement(name = "Authorization"))
    @GetMapping("/point")
    public ResponseEntity<PointBalanceResponse> getWalletPoint(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(walletService.getPointBalance(principal.getUsername()));
    }
}