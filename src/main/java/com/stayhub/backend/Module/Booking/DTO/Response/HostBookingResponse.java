package com.stayhub.backend.Module.Booking.DTO.Response;

import com.stayhub.backend.Common.Util.BookingStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record HostBookingResponse(
        String bookingCode,
        String guestName,       // Tên khách hàng
        String propertyName,    // Tên chỗ ở
        LocalDate checkInDate,  // Ngày đến
        LocalDate checkOutDate, // Ngày đi
        Integer totalGuests,    // Số lượng khách
        BigDecimal finalAmount, // Tổng tiền đơn hàng
        BigDecimal amountPaid,  // Số tiền ĐÃ thanh toán (Cọc hoặc toàn bộ)
        Boolean isFullyPaid,    // Trạng thái thanh toán đủ
        BookingStatus status,   // Trạng thái Booking
        LocalDateTime createdAt
) {
}
