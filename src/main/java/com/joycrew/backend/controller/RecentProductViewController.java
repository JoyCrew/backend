package com.joycrew.backend.controller;

import com.joycrew.backend.dto.RecentViewedProductResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.RecentProductViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recent Views", description = "APIs for recording and retrieving recently viewed products")
@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
public class RecentProductViewController {

  private final RecentProductViewService recentProductViewService;

  @Operation(summary = "Record a recent view", description = "Records a product as recently viewed by the current user.")
  @PostMapping("/{productId}")
  public ResponseEntity<Void> recordView(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable Long productId
  ) {
    Long employeeId = principal.getEmployee().getEmployeeId();
    recentProductViewService.recordView(employeeId, productId);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Get recent views",
      description = "Returns the current user's recently viewed products within the last 3 months.",
      parameters = @Parameter(name = "limit", description = "Max items to return (default 20, max 100)", example = "20")
  )
  @GetMapping
  public ResponseEntity<List<RecentViewedProductResponse>> getRecentViews(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(required = false) Integer limit
  ) {
    Long employeeId = principal.getEmployee().getEmployeeId();
    return ResponseEntity.ok(recentProductViewService.getRecentViews(employeeId, limit));
  }
}
