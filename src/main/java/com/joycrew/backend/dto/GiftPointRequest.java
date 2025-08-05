package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GiftPointRequest(
        @NotNull(message = "받는 사람 ID는 필수입니다.")
        Long receiverId,

        @NotNull(message = "포인트는 필수입니다.")
        @Min(value = 1, message = "포인트는 1 이상이어야 합니다.")
        int points,

        @Size(max = 255, message = "메시지는 255자를 초과할 수 없습니다.")
        String message,

        @NotNull(message = "태그는 필수입니다.")
        @Size(min = 1, max = 3, message = "태그는 최소 1개, 최대 3개까지 선택 가능합니다.")
        List<Tag> tags
) {}
