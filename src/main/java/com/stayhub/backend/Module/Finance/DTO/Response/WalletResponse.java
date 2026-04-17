package com.stayhub.backend.Module.Finance.DTO.Response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record WalletResponse(
        BigDecimal availableBalance,
        BigDecimal pendingBalance,
        BigDecimal debtBalance,
        String currency
) {
}
