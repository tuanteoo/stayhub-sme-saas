package com.stayhub.backend.Module.Finance.Service;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Finance.DTO.Response.TransactionResponse;
import com.stayhub.backend.Module.Finance.DTO.Response.WalletResponse;

import java.math.BigDecimal;

public interface WalletService {
    void processBookingPaymentSuccess(Booking booking, BigDecimal amountPaid);
    WalletResponse getMyWallet(Long hostId);
    PageResponse<TransactionResponse> getMyTransactions(Long hostId, String balanceAffected, int page, int size);
}
