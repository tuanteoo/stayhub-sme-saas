package com.stayhub.backend.Common.DTO.Response;

import lombok.Builder;

@Builder
public record PresignedUrlResponse(
        String presignedUrl,
        String publicUrl
) {
}
