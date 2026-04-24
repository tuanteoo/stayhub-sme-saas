package com.stayhub.backend.Module.Finance.Service;

import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Finance.Model.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    String createBookingVNPayUrl(String bookingCode, HttpServletRequest request);
    Map<String, String> processVnPayIpn(HttpServletRequest request);
    String createSubscriptionVNPayUrl(Long planId, Long hostId, HttpServletRequest request);
    boolean refundVnPayTransaction(Payment originalPayment, BigDecimal refundAmount);
}
