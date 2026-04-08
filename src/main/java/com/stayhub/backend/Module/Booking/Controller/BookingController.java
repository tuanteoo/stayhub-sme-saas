package com.stayhub.backend.Module.Booking.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.BookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.HostBookingResponse;
import com.stayhub.backend.Module.Booking.Service.BookingService;
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

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Guest - Tạo đơn đặt phòng")
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
    @GetMapping("/host")
    public ResponseEntity<ResponseData<PageResponse<HostBookingResponse>>> getBookingsForHost(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long hostId = customUserDetails.getUser().getId();

        PageResponse<HostBookingResponse> response = bookingService.getBookingsForHost(hostId, page, size);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách đơn đặt phòng thành công", response));
    }
}
