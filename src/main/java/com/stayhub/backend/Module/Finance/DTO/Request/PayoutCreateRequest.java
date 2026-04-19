package com.stayhub.backend.Module.Finance.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PayoutCreateRequest(
        @Schema(description = "Số tiền rút", example = "1000000")
        @NotNull(message = "Số tiền rút không được để trống")
        BigDecimal amountPayout,

        @Schema(description = "ID tài khoản ngân hàng", example = "1")
        @NotNull(message = "ID tài khoản ngân hàng không được để trống")
        Integer bankAccountId
) {
}
