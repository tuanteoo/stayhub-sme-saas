package com.stayhub.backend.Module.Booking.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DisputeAdminResponse(
        Long id,
        String bookingCode,
        Long creatorId,
        String creatorEmail,
        String creatorRole,
        String creatorName,
        String reason,
        String evidenceImageUrls,
        String status,
        LocalDateTime createdAt
) {
}
