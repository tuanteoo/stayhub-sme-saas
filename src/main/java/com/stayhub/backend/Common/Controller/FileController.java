package com.stayhub.backend.Common.Controller;

import com.stayhub.backend.Common.DTO.Request.PresignedUrlRequest;
import com.stayhub.backend.Common.DTO.Response.PresignedUrlResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Common.Service.S3PresignedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final S3PresignedService s3PresignedService;

    @GetMapping("/presigned-url")
    public ResponseData<PresignedUrlResponse> getPresignedUrl(
            @RequestParam(defaultValue = ".jpg") String extension,
            @RequestParam(defaultValue = "image/jpeg") String contentType) {

        PresignedUrlResponse response = s3PresignedService.generatePresignedUrl(extension, contentType);
        return new ResponseData<>(200, "Tạo Presigned URL thành công", response);
    }

    @PostMapping("/presigned-urls/batch")
    public ResponseData<List<PresignedUrlResponse>> getMultiplePresignedUrls(
            @RequestBody PresignedUrlRequest request) {

        List<PresignedUrlResponse> responses = s3PresignedService.generateMultiplePresignedUrls(request.files());

        return new ResponseData<>(200, "Tạo danh sách Presigned URL thành công", responses);
    }
}
