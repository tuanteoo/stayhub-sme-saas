package com.stayhub.backend.Module.Booking.DTO.Response;

import com.stayhub.backend.Common.Util.BookingStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record GuestBookingResponse(
        String bookingCode,
        String thumbnailUrl,
        String propertyName,
        String propertyAddress,
        String hostEmail,
        String hostName,
        String hostPhone,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime createdAt
) {
}
