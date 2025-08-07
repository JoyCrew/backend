package com.joycrew.backend.controller;

import com.joycrew.backend.dto.TransactionHistoryResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Transaction History", description = "API for retrieving point transaction history")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;

    @Operation(summary = "Get point transaction history", security = @SecurityRequirement(name = "Authorization"))
    @GetMapping
    public ResponseEntity<List<TransactionHistoryResponse>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TransactionHistoryResponse> history = transactionHistoryService.getTransactionHistory(principal.getUsername());
        return ResponseEntity.ok(history);
    }
}