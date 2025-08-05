package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "포인트 선물 요청 DTO")
public record GiftPointRequest(

        @Schema(description = "포인트를 받을 직원의 ID", example = "2")
        @NotNull(message = "받는 사람 ID는 필수입니다.")
        Long receiverId,

        @Schema(description = "선물할 포인트 수", example = "50", minimum = "1")
        @NotNull(message = "포인트는 필수입니다.")
        @Min(value = 1, message = "포인트는 1 이상이어야 합니다.")
        int points,

        @Schema(description = "응원의 메시지 (선택 사항, 최대 255자)", example = "이번 프로젝트 수고하셨어요!")
        @Size(max = 255, message = "메시지는 255자를 초과할 수 없습니다.")
        String message,

        @Schema(description = "포인트 선물에 함께 전달할 태그 목록 (최소 1개, 최대 3개)", example = "[\"TEAMWORK\", \"LEADERSHIP\"]")
        @NotNull(message = "태그는 필수입니다.")
        @Size(min = 1, max = 3, message = "태그는 최소 1개, 최대 3개까지 선택 가능합니다.")
        List<Tag> tags
) {}
