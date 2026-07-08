package com.stayhub.backend.Module.Review.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import com.stayhub.backend.Module.Review.DTO.Request.HostReplyRequest;
import com.stayhub.backend.Module.Review.DTO.Request.ReviewCreateRequest;
import com.stayhub.backend.Module.Review.Service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Quản lý đánh giá và phản hồi")
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "GUEST - Gửi đánh giá cho chuyến đi đã hoàn thành")
    @PostMapping("/guest/{bookingCode}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ResponseData<Void>> createReview(
            @PathVariable String bookingCode,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.createReview(userDetails.getUser().getId(), bookingCode, request);
        return ResponseEntity.ok(new ResponseData<>(201, "Gửi đánh giá thành công"));
    }

    @Operation(summary = "HOST - Trả lời đánh giá của khách hàng")
    @PutMapping("/host/{reviewId}/reply")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    public ResponseEntity<ResponseData<Void>> replyToReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody HostReplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.replyToReview(userDetails.getUser().getId(), reviewId, request);
        return ResponseEntity.ok(new ResponseData<>(200, "Gửi phản hồi đánh giá thành công"));
    }
}
