package com.stayhub.backend.Module.Finance.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BalanceAffected;
import com.stayhub.backend.Common.Util.TransactionStatus;
import com.stayhub.backend.Common.Util.TransactionType;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Finance.DTO.Response.TransactionResponse;
import com.stayhub.backend.Module.Finance.DTO.Response.WalletResponse;
import com.stayhub.backend.Module.Finance.Model.Transaction;
import com.stayhub.backend.Module.Finance.Model.Wallet;
import com.stayhub.backend.Module.Finance.Repository.TransactionRepository;
import com.stayhub.backend.Module.Finance.Repository.WalletRepository;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public WalletResponse getMyWallet(Long hostId) {
        Wallet wallet = walletRepository.findByUser_Id(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa được khởi tạo ví điện tử. Vui lòng liên hệ Admin."));

        return WalletResponse.builder()
                .availableBalance(wallet.getAvailableBalance())
                .pendingBalance(wallet.getPendingBalance())
                .debtBalance(wallet.getDebtBalance())
                .currency(wallet.getCurrency())
                .build();
    }

    @Override
    public PageResponse<TransactionResponse> getMyTransactions(Long hostId, String balanceAffected, int pageNo, int pageSize) {
        Wallet wallet = walletRepository.findByUser_Id(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa được khởi tạo ví điện tử."));

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Transaction> transactionPage;

        if (balanceAffected != null && !balanceAffected.trim().isEmpty()) {
            try {
                BalanceAffected type = BalanceAffected.valueOf(balanceAffected.toUpperCase());
                transactionPage = transactionRepository.findByWallet_IdAndBalanceAffectedOrderByCreatedAtDesc(wallet.getId(), type, pageable);
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Loại rổ tiền không hợp lệ (Chỉ nhận AVAILABLE, PENDING, DEBT)");
            }
        } else {
            transactionPage = transactionRepository.findByWallet_IdOrderByCreatedAtDesc(wallet.getId(), pageable);
        }

        List<TransactionResponse> responses = transactionPage.getContent().stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .amount(t.getAmount())
                        .balanceAffected(t.getBalanceAffected() != null ? t.getBalanceAffected().name() : null)
                        .type(t.getType().name())
                        .status(t.getStatus().name())
                        .description(t.getDescription())
                        .bookingCode(t.getBooking() != null ? t.getBooking().getBookingCode() : null)
                        .createdAt(t.getCreatedAt())
                        .build())
                .toList();

        return PageResponse.<TransactionResponse>builder()
                .pageNo(pageNo)
                .pageSize(pageable.getPageSize())
                .totalPage(transactionPage.getTotalPages())
                .totalElements(transactionPage.getTotalElements())
                .items(responses)
                .build();
    }
}
