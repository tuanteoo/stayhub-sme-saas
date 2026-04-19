package com.stayhub.backend.Module.Finance.DTO.Response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PayoutResponse(
        Long id,
        String hostEmail,
        String hostName,
        BigDecimal amount,
        String bankCode,
        String accountNumber,
        String accountHolderName,
        String status,
        String adminNote,
        String bankTransactionRef,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {
}
