package com.stayhub.backend.Module.Booking.Service;

import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.BookingResponse;

public interface BookingService {
    String createBooking(BookingCreateRequest request, Long id);
}
