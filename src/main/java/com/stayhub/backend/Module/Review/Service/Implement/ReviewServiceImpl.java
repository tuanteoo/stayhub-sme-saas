package com.stayhub.backend.Module.Review.Service.Implement;

import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Property.Model.Property;
import com.stayhub.backend.Module.Property.Repository.PropertyRepository;
import com.stayhub.backend.Module.Review.DTO.Request.HostReplyRequest;
import com.stayhub.backend.Module.Review.DTO.Request.ReviewCreateRequest;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Review.Model.Review;
import com.stayhub.backend.Module.Review.Model.ReviewImage;
import com.stayhub.backend.Module.Review.Repository.ReviewRepository;
import com.stayhub.backend.Module.Review.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import com.stayhub.backend.Module.Review.Event.ReviewCreatedEvent;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createReview(Long guestId, String bookingCode, ReviewCreateRequest request) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đơn đặt phòng."));

        if (!booking.getUser().getId().equals(guestId)) {
            throw new AuthorizationDeniedException("Bạn không có quyền đánh giá đơn đặt phòng này.");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidDataException("Chỉ được phép gửi đánh giá khi chuyến đi đã hoàn thành.");
        }

        if (reviewRepository.existsByBooking_Id(booking.getId())) {
            throw new InvalidDataException("Bạn đã đánh giá đơn đặt phòng này rồi.");
        }

        Property property = booking.getProperty();

        Review review = Review.builder()
                .booking(booking)
                .user(booking.getUser())
                .property(booking.getProperty())
                .rating(request.rating())
                .comment(request.comment())
                .isVisible(true)
                .build();

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<ReviewImage> images = request.imageUrls().stream()
                    .map(url -> ReviewImage.builder().review(review).imageUrl(url).build())
                    .toList();
            review.getImages().addAll(images);
        }

        reviewRepository.save(review);
        
        applicationEventPublisher.publishEvent(new ReviewCreatedEvent(this, review));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void replyToReview(Long hostId, Long reviewId, HostReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đánh giá."));

        Long actualHostId = review.getProperty().getHost().getId();
        if (!actualHostId.equals(hostId)) {
            throw new AuthorizationDeniedException("Bạn không có quyền phản hồi bài đánh giá này.");
        }

        if (review.getHostReply() != null && !review.getHostReply().isBlank()) {
            throw new InvalidDataException("Bạn đã phản hồi bài đánh giá này trước đó, không thể gửi thêm phản hồi.");
        }

        review.setHostReply(request.reply());
        review.setReplyTime(LocalDateTime.now());

        reviewRepository.save(review);
    }
}
