package com.stayhub.backend.Common.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PresignedUrlRequest(

        @Schema(description = "Danh sách tệp tin cần tạo URL ký trước")
        @NotEmpty(message = "Danh sách tệp tin không được để trống")
        @Valid List<FileMetadata> files
) {
    public record FileMetadata(

            @Schema(description = "Phần mở rộng của tệp tin", example = ".png")
            @NotBlank(message = "Phần mở rộng tệp tin không được để trống")
            String extension,

            @Schema(description = "Định dạng MIME của tệp tin cần tạo URL ký trước", example = "image/png")
            @NotBlank(message = "Định dạng MIME của tệp tin không được để trống")
            String contentType
    ) {}
}
