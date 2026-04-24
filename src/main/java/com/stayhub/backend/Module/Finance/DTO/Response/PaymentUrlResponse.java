package com.stayhub.backend.Module.Finance.DTO.Response;

import lombok.Builder;

@Builder
public record PaymentUrlResponse(
        String paymentUrl, String paymentMethod
) {
}
