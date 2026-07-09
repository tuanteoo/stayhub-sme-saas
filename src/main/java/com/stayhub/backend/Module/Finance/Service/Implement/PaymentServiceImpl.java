package com.stayhub.backend.Module.Finance.Service.Implement;

import com.stayhub.backend.Common.Util.PaymentMethod;
import com.stayhub.backend.Module.Finance.Model.Payment;
import com.stayhub.backend.Module.Finance.Service.PaymentService;
import com.stayhub.backend.Module.Finance.Service.Strategy.PaymentStrategyFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentStrategyFactory strategyFactory;

    @Override
    public String createBookingPaymentUrl(PaymentMethod method, String bookingCode, HttpServletRequest request) {
        return strategyFactory.getStrategy(method).createBookingPaymentUrl(bookingCode, request);
    }

    @Override
    public Map<String, String> processIpn(PaymentMethod method, HttpServletRequest request) {
        return strategyFactory.getStrategy(method).processIpn(request);
    }

    @Override
    public String createSubscriptionPaymentUrl(PaymentMethod method, Long planId, Long hostId, HttpServletRequest request) {
        return strategyFactory.getStrategy(method).createSubscriptionPaymentUrl(planId, hostId, request);
    }

    @Override
    public boolean refundTransaction(PaymentMethod method, Payment originalPayment, BigDecimal refundAmount) {
        return strategyFactory.getStrategy(method).refundTransaction(originalPayment, refundAmount);
    }
}