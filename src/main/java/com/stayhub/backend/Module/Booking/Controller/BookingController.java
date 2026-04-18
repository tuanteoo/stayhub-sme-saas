package com.stayhub.backend.Module.Booking.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Request.DisputeCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.BookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.GuestBookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.HostBookingResponse;
import com.stayhub.backend.Module.Booking.Service.BookingService;
import com.stayhub.backend.Module.Booking.Service.DisputeService;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@Tag(name = "Booking", description = "API Quản lý Đặt phòng")
public class BookingController {
    private final BookingService bookingService;
    private final DisputeService disputeService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "USER - Tạo đơn đặt phòng")
    public ResponseEntity<ResponseData<String>> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String bookingCode = bookingService.createBooking(request, customUserDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseData<>(
                        HttpStatus.CREATED.value(),
                        "Tạo đơn đặt phòng thành công",
                        bookingCode
                )
        );
    }

    @Operation(summary = "HOST - Lấy danh sách đơn đặt phòng")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/host")
    public ResponseEntity<ResponseData<PageResponse<HostBookingResponse>>> getBookingsForHost(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long hostId = customUserDetails.getUser().getId();

        PageResponse<HostBookingResponse> response = bookingService.getBookingsForHost(hostId, page, size);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách đơn đặt phòng thành công", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "USER - Xem lịch sử các chuyến đi")
    public ResponseEntity<ResponseData<PageResponse<GuestBookingResponse>>> getMyTrips(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        PageResponse<GuestBookingResponse> response = bookingService.getBookingForGuest(currentUser.getUser().getId(), page, size);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách chuyến đi thành công", response));
    }

    @Operation(summary = "USER - Hủy đơn đặt phòng")
    @PutMapping("/cancel/{bookingCode}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ResponseData<String>> cancelBookingByGuest(
            @PathVariable String bookingCode,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long guestId = userDetails.getUser().getId();

        String message = bookingService.cancelBookingByGuest(guestId, bookingCode);

        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Hủy đơn đặt phòng thành công",
                message
        ));
    }

    @Operation(summary = "HOTS - Xác nhận khách đã Check-in")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PutMapping("/{bookingCode}/check-in")
    public ResponseEntity<ResponseData<String>> hostCheckIn(
            @PathVariable String bookingCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String message = bookingService.hostCheckIn(userDetails.getUser().getId(), bookingCode);
        return ResponseEntity.ok(new ResponseData<>(200, message, null));
    }

    @Operation(summary = "Chủ nhà xác nhận khách đã Check-out")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PutMapping("/{bookingCode}/check-out")
    public ResponseEntity<ResponseData<String>> hostCheckOut(
            @PathVariable String bookingCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String message = bookingService.hostCheckOut(userDetails.getUser().getId(), bookingCode);
        return ResponseEntity.ok(new ResponseData<>(200, message, null));
    }

    @Operation(summary = "Khách hàng xác nhận hoàn thành chuyến đi sớm")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/{bookingCode}/complete")
    public ResponseEntity<ResponseData<String>> guestCompleteBooking(
            @PathVariable String bookingCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String message = bookingService.guestCompleteBooking(userDetails.getUser().getId(), bookingCode);
        return ResponseEntity.ok(new ResponseData<>(200, message, null));
    }

    @Operation(summary = "Tạo khiếu nại đơn hàng (Dành cho cả Chủ nhà và Khách thuê)")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST')")
    @PostMapping("/{bookingCode}/disputes")
    public ResponseEntity<ResponseData<Object>> createDispute(
            @PathVariable String bookingCode,
            @Valid @RequestBody DisputeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String message = disputeService.createDispute(userDetails.getUser().getId(), bookingCode, request);

        return ResponseEntity.ok(new ResponseData<>(201, message));
    }
}
