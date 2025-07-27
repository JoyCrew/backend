package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작업 성공 응답 DTO")
public record SuccessResponse(
        @Schema(description = "성공 메시지", example = "작업이 성공적으로 완료되었습니다.")
        String message
) {
    public static SuccessResponse defaultSuccess() {
        return new SuccessResponse("성공적으로 처리되었습니다.");
    }
}
