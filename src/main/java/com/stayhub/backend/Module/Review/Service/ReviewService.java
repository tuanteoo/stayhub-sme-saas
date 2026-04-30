package com.stayhub.backend.Module.Review.Service;

import com.stayhub.backend.Module.Review.DTO.Request.HostReplyRequest;
import com.stayhub.backend.Module.Review.DTO.Request.ReviewCreateRequest;

public interface ReviewService {
    void createReview(Long guestId, String bookingCode, ReviewCreateRequest request);
    void replyToReview(Long hostId, Long reviewId, HostReplyRequest request);
}
