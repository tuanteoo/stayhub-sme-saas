package com.stayhub.backend.Common.Service;

import com.stayhub.backend.Common.DTO.Response.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3PresignedService {
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public PresignedUrlResponse generatePresignedUrl(String extension, String contentType) {
        // 1. Tạo tên file độc nhất
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // 2. Định nghĩa yêu cầu Upload
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .contentType(contentType)
                .build();

        // 3. Ký tên vào URL, cho phép vé này có hiệu lực trong đúng 15 phút
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        // 4. Lấy URL đã ký và URL public
        String presignedUrl = presignedRequest.url().toString();
        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, uniqueFileName);

        return new PresignedUrlResponse(presignedUrl, publicUrl);
    }
}
