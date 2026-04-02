package com.stayhub.backend.Module.Booking.Controller;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Booking.Service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "API Thanh toán với VNPAY")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/vnpay/create-url")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Lấy URL thanh toán VNPAY cho đơn đặt phòng")
    public ResponseEntity<ResponseData<String>> createVNPayUrl(
            @RequestParam String bookingCode,
            HttpServletRequest request) {

        String paymentUrl = paymentService.createVNPayUrl(bookingCode, request);

        return ResponseEntity.ok(
                new ResponseData<>(200, "Tạo URL thanh toán thành công", paymentUrl)
        );
    }

    @GetMapping("/vnpay/ipn")
    @Operation(summary = "VNPAY IPN Webhook")
    public ResponseEntity<Map<String, String>> processVNPayIPN(HttpServletRequest request) {
        Map<String, String> response = paymentService.processVnPayIpn(request);
        return ResponseEntity.ok(response);
    }
}
