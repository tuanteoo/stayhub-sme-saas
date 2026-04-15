package com.stayhub.backend.Module.Finance.Service.Implement;

import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BalanceAffected;
import com.stayhub.backend.Common.Util.TransactionStatus;
import com.stayhub.backend.Common.Util.TransactionType;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Finance.Model.Transaction;
import com.stayhub.backend.Module.Finance.Model.Wallet;
import com.stayhub.backend.Module.Finance.Repository.TransactionRepository;
import com.stayhub.backend.Module.Finance.Repository.WalletRepository;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    private final BookingRepository bookingRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void processBookingPaymentSuccess(Booking booking, BigDecimal amountPaid) {
        User host = booking.getBookingRooms().get(0).getRoom().getProperty().getHost();

        Wallet wallet = walletRepository.findByUser_Id(host.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Chưa khởi tạo ví cho Chủ nhà này"));

        BigDecimal commission = booking.getPlatformCommission() != null ? booking.getPlatformCommission() : BigDecimal.ZERO;

        BigDecimal netIncome = amountPaid.subtract(commission);

        wallet.setPendingBalance(wallet.getPendingBalance().add(netIncome));
        walletRepository.save(wallet);

        List<Transaction> transactions = new ArrayList<>();

        transactions.add(Transaction.builder()
                .wallet(wallet)
                .booking(booking)
                .amount(amountPaid)
                .balanceAffected(BalanceAffected.PENDING)
                .type(TransactionType.BOOKING_INCOME)
                .status(TransactionStatus.SUCCESS)
                .description("Nhận thanh toán (Cọc/Toàn bộ) từ khách cho đơn " + booking.getBookingCode())
                .build());

        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            transactions.add(Transaction.builder()
                    .wallet(wallet)
                    .booking(booking)
                    .amount(commission.negate())
                    .balanceAffected(BalanceAffected.PENDING)
                    .type(TransactionType.SYSTEM_FEE)
                    .status(TransactionStatus.SUCCESS)
                    .description("Thu phí dịch vụ nền tảng cho đơn " + booking.getBookingCode())
                    .build());
        }

        transactionRepository.saveAll(transactions);

        log.info("Đã chốt dòng tiền đơn {}. Cộng {} VND vào TẠM TÍNH của Host {}",
                booking.getBookingCode(), netIncome, host.getId());
    }
}
