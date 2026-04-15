package com.stayhub.backend.Module.Finance.Service;

import com.stayhub.backend.Module.Booking.Model.Booking;

import java.math.BigDecimal;

public interface WalletService {
    void processBookingPaymentSuccess(Booking booking, BigDecimal amountPaid);
}
