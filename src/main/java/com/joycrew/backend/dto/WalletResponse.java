package com.joycrew.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "지갑 잔액 응답 DTO")
public class WalletResponse {

    @Schema(description = "현재 잔액", example = "12000")
    private Integer balance;
}
