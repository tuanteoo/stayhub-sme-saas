package com.stayhub.backend.Common.Controller;

import com.stayhub.backend.Common.DTO.Request.PresignedUrlRequest;
import com.stayhub.backend.Common.DTO.Response.PresignedUrlResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Common.Service.S3PresignedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final S3PresignedService s3PresignedService;

    @Operation(summary = "HOST_USER - Tạo 1 Presigned URL để tải lên file lên S3")
    @PreAuthorize("hasAuthority('ROLE_HOST') or hasAuthority('ROLE_USER')")
    @PostMapping("/presigned-url")
    public ResponseData<PresignedUrlResponse> getPresignedUrl(
            @RequestBody PresignedUrlRequest.FileMetadata fileData) {

        PresignedUrlResponse response = s3PresignedService.generatePresignedUrl(
                fileData.extension(),
                fileData.contentType()
        );

        return new ResponseData<>(200, "Tạo Presigned URL thành công", response);
    }

    @Operation(summary = "HOST_USER - Tạo nhiều Presigned URL để tải lên nhiều file lên S3")
    @PreAuthorize("hasAuthority('ROLE_HOST') or hasAuthority('ROLE_USER')")
    @PostMapping("/presigned-urls/batch")
    public ResponseData<List<PresignedUrlResponse>> getMultiplePresignedUrls(
            @RequestBody PresignedUrlRequest request) {

        List<PresignedUrlResponse> responses = s3PresignedService.generateMultiplePresignedUrls(request.files());

        return new ResponseData<>(200, "Tạo danh sách Presigned URL thành công", responses);
    }
}
