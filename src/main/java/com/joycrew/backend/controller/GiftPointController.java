package com.joycrew.backend.controller;

import com.joycrew.backend.dto.GiftPointRequest;
import com.joycrew.backend.dto.SuccessResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.GiftPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "포인트 선물", description = "동료 간 포인트 선물 API")
@RestController
@RequestMapping("/api/gift-points")
@RequiredArgsConstructor
public class GiftPointController {

    private final GiftPointService giftPointService;

    @Operation(summary = "동료에게 포인트 선물하기", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping
    public ResponseEntity<SuccessResponse> giftPoints(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GiftPointRequest request
    ) {
        giftPointService.giftPointsToColleague(principal.getUsername(), request);
        return ResponseEntity.ok(new SuccessResponse("포인트를 성공적으로 보냈습니다."));
    }
}
