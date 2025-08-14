package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PointStatisticsResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics", description = "포인트 및 활동 통계 API")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

  private final StatisticsService statisticsService;

  @Operation(summary = "내 포인트 통계 조회", description = "로그인된 사용자의 주고받은 포인트 및 태그 통계를 조회합니다.", security = @SecurityRequirement(name = "Authorization"))
  @GetMapping("/me")
  public ResponseEntity<PointStatisticsResponse> getMyStatistics(
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    PointStatisticsResponse stats = statisticsService.getPointStatistics(principal.getUsername());
    return ResponseEntity.ok(stats);
  }
}
