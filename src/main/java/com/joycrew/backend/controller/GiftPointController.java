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

@Tag(name = "Gift Points", description = "APIs for gifting points between colleagues")
@RestController
@RequestMapping("/api/gift-points")
@RequiredArgsConstructor
public class GiftPointController {

  private final GiftPointService giftPointService;

  @Operation(summary = "Gift points to a colleague", security = @SecurityRequirement(name = "Authorization"))
  @PostMapping
  public ResponseEntity<SuccessResponse> giftPoints(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody GiftPointRequest request
  ) {
    giftPointService.giftPointsToColleague(principal.getUsername(), request);
    return ResponseEntity.ok(new SuccessResponse("Points sent successfully."));
  }
}
