package com.stayhub.backend.Common.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {
    String createBookingVNPayUrl(String bookingCode, HttpServletRequest request);
    Map<String, String> processVnPayIpn(HttpServletRequest request);
    String createSubscriptionVNPayUrl(Long planId, Long hostId, HttpServletRequest request);
}
