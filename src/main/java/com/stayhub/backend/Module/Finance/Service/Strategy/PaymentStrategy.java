package com.stayhub.backend.Module.Finance.Service.Strategy;

import com.stayhub.backend.Common.Util.PaymentMethod;
import com.stayhub.backend.Module.Finance.Model.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentStrategy {
    PaymentMethod getPaymentMethod();
    String createBookingPaymentUrl(String bookingCode, HttpServletRequest request);
    String createSubscriptionPaymentUrl(Long planId, Long hostId, HttpServletRequest request);
    Map<String, String> processIpn(HttpServletRequest request);
    boolean refundTransaction(Payment originalPayment, BigDecimal refundAmount);
}
