package com.stayhub.backend.Module.Booking.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.BookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.GuestBookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.HostBookingResponse;

public interface BookingService {
    String createBooking(BookingCreateRequest request, Long id);
    PageResponse<HostBookingResponse> getBookingsForHost(Long hostId, int page, int size);
    PageResponse<GuestBookingResponse> getBookingForGuest(Long guestId, int page, int size);
}
