package com.stayhub.backend.Module.Finance.DTO.Response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String balanceAffected,
        String type,
        String status,
        String description,
        String bookingCode,
        LocalDateTime createdAt
) {
}
