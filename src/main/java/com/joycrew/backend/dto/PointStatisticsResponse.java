package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "포인트 및 태그 통계 응답 DTO")
public record PointStatisticsResponse(
  @Schema(description = "총 받은 포인트 합계")
  Integer totalPointsReceived,

  @Schema(description = "총 보낸 포인트 합계")
  Integer totalPointsSent,

  @Schema(description = "받은 태그 종류 및 횟수 (Enum 순서대로)")
  List<Long> tagCounts,

  @Schema(description = "받은 포인트 거래 내역")
  List<TransactionHistoryResponse> receivedTransactions,

  @Schema(description = "보낸 포인트 거래 내역")
  List<TransactionHistoryResponse> sentTransactions
) {}