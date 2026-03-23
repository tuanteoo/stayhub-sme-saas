package com.stayhub.backend.Common.Controller;

import com.stayhub.backend.Common.DTO.Response.PresignedUrlResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Common.Service.S3PresignedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final S3PresignedService s3PresignedService;

    @GetMapping("/presigned-url")
    // @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseData<PresignedUrlResponse> getPresignedUrl(
            @RequestParam(defaultValue = ".jpg") String extension,
            @RequestParam(defaultValue = "image/jpeg") String contentType) {

        PresignedUrlResponse response = s3PresignedService.generatePresignedUrl(extension, contentType);
        return new ResponseData<>(200, "Tạo Presigned URL thành công", response);
    }
}
