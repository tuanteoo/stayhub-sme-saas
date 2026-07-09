package com.stayhub.backend.Module.Finance.Service;

import com.stayhub.backend.Common.Util.PaymentMethod;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Finance.Model.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    String createBookingPaymentUrl(PaymentMethod method, String bookingCode, HttpServletRequest request);
    Map<String, String> processIpn(PaymentMethod method, HttpServletRequest request);
    String createSubscriptionPaymentUrl(PaymentMethod method, Long planId, Long hostId, HttpServletRequest request);
    boolean refundTransaction(PaymentMethod method, Payment originalPayment, BigDecimal refundAmount);
}
