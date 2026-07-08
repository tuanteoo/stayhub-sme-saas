package com.stayhub.backend.Module.Review.DTO.Response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReviewResponse(
        Long id,
        String guestName,
        String guestAvatarUrl,
        Integer rating,
        String comment,
        List<String> imageUrls,
        String hostReply,
        LocalDateTime replyTime,
        LocalDateTime createdAt
) {
}
