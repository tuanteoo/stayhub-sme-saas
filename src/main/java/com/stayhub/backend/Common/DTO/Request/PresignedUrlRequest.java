package com.stayhub.backend.Common.DTO.Request;

import java.util.List;

public record PresignedUrlRequest(
        List<FileMetadata> files
) {
    public record FileMetadata(
            String extension,
            String contentType
    ) {}
}
