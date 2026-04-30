package com.stayhub.backend.Module.Booking.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.BookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.GuestBookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.HostBookingResponse;
import com.stayhub.backend.Module.Booking.Model.Booking;

public interface BookingService {
    String createBooking(BookingCreateRequest request, Long id);
    PageResponse<HostBookingResponse> getBookingsForHost(Long hostId, String status, int page, int size, String sortBy, String sortDir);
    PageResponse<GuestBookingResponse> getBookingForGuest(Long guestId, String status, int page, int size, String sortBy, String sortDir);
    void releaseBookingInternal(Booking booking, BookingStatus targetStatus, Long cancelledBy);
    String cancelBookingByGuest(Long guestId, String bookingCode);
    String hostCheckIn(Long hostId, String bookingCode);
    String hostCheckOut(Long hostId, String bookingCode);
    String guestCompleteBooking(Long guestId, String bookingCode);
}
