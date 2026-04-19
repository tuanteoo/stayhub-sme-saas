package com.stayhub.backend.Module.Identity.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HostApplicationResponse(
        Long hostId,
        String hostCode,
        String fullName,
        String email,
        String businessPhone,
        String supportEmail,
        String identityCardNumber,
        String identityCardFrontUrl,
        String identityCardBackUrl,
        String businessLicenseNumber,
        String businessLicenseUrl,
        String onboardingStatus,
        String reviewNote,
        LocalDateTime createdAt
) {
}
