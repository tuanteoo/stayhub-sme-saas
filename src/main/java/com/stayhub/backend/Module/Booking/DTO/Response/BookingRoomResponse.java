package com.stayhub.backend.Module.Booking.DTO.Response;

import java.math.BigDecimal;

public record BookingRoomResponse(
        Long roomId,
        String roomName,
        Integer numGuests,
        BigDecimal priceAtBooking
) {
}
