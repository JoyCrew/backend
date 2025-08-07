package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Gift Points Request DTO")
public record GiftPointRequest(

        @Schema(description = "ID of the employee who will receive the points", example = "2")
        @NotNull(message = "Receiver ID is required.")
        Long receiverId,

        @Schema(description = "Number of points to gift", example = "50", minimum = "1")
        @NotNull(message = "Points are required.")
        @Min(value = 1, message = "Points must be at least 1.")
        int points,

        @Schema(description = "Encouragement message (optional, max 255 chars)", example = "Great job on the project!")
        @Size(max = 255, message = "Message cannot exceed 255 characters.")
        String message,

        @Schema(description = "List of tags to send with the points (min 1, max 3)", example = "[\"TEAMWORK\", \"LEADERSHIP\"]")
        @NotNull(message = "Tags are required.")
        @Size(min = 1, max = 3, message = "Between 1 and 3 tags can be selected.")
        List<Tag> tags
) {}