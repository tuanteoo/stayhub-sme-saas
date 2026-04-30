package com.stayhub.backend.Module.Booking.DTO.Response;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Review.DTO.Response.ReviewResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record HostBookingResponse(
        String bookingCode,
        String guestName,
        String propertyName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer totalGuests,
        BigDecimal finalAmount,
        BigDecimal amountPaid,
        Boolean isFullyPaid,
        BookingStatus status,
        LocalDateTime createdAt,
        ReviewResponse review
) {
}
