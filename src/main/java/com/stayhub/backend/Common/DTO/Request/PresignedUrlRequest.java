package com.stayhub.backend.Common.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PresignedUrlRequest(
        @NotEmpty(message = "Danh sách tệp tin không được để trống")
        @Valid List<FileMetadata> files
) {
    public record FileMetadata(
            @NotBlank(message = "Phần mở rộng tệp tin không được để trống")
            String extension,

            @NotBlank(message = "Định dạng MIME của tệp tin không được để trống")
            String contentType
    ) {}
}
