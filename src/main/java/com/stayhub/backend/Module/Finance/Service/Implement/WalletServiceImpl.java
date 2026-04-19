package com.stayhub.backend.Module.Finance.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.*;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Finance.DTO.Request.PayoutCreateRequest;
import com.stayhub.backend.Module.Finance.DTO.Request.PayoutProcessRequest;
import com.stayhub.backend.Module.Finance.DTO.Response.TransactionResponse;
import com.stayhub.backend.Module.Finance.DTO.Response.WalletResponse;
import com.stayhub.backend.Module.Finance.Model.*;
import com.stayhub.backend.Module.Finance.Repository.*;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PayoutRepository payoutRepository;

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

        int validPage = (pageNo <= 0) ? 1 : pageNo;
        int pageNumber = validPage - 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
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
                .pageNo(validPage)
                .pageSize(transactionPage.getSize())
                .totalPage(transactionPage.getTotalPages())
                .totalElements(transactionPage.getTotalElements())
                .items(responses)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unlockPendingBalance(Booking booking) {
        User host = booking.getBookingRooms().get(0).getRoom().getProperty().getHost();
        Wallet wallet = walletRepository.findByUser_Id(host.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví của Chủ nhà"));

        Payment payment = paymentRepository.findFirstByBooking_IdAndPaymentStatusOrderByCreatedAtDesc(
                        booking.getId(), PaymentStatus.COMPLETED)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy giao dịch thanh toán gốc."));

        BigDecimal commission = booking.getPlatformCommission() != null ? booking.getPlatformCommission() : BigDecimal.ZERO;
        BigDecimal netIncome = payment.getAmount().subtract(commission);

        if (netIncome.compareTo(BigDecimal.ZERO) > 0) {
            wallet.setPendingBalance(wallet.getPendingBalance().subtract(netIncome));

            BigDecimal currentDebt = wallet.getDebtBalance();
            BigDecimal amountToDeductForDebt = BigDecimal.ZERO;

            if (currentDebt.compareTo(BigDecimal.ZERO) > 0) {
                amountToDeductForDebt = currentDebt.min(netIncome);
                wallet.setDebtBalance(currentDebt.subtract(amountToDeductForDebt));
            }

            BigDecimal finalAvailableIncome = netIncome.subtract(amountToDeductForDebt);
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(finalAvailableIncome));

            walletRepository.save(wallet);

            List<Transaction> transactions = new ArrayList<>();

            if (finalAvailableIncome.compareTo(BigDecimal.ZERO) > 0) {
                transactions.add(Transaction.builder()
                        .wallet(wallet)
                        .booking(booking)
                        .amount(finalAvailableIncome)
                        .balanceAffected(BalanceAffected.AVAILABLE)
                        .type(TransactionType.BOOKING_INCOME)
                        .status(TransactionStatus.SUCCESS)
                        .description("Mở khóa doanh thu đơn " + booking.getBookingCode() + (amountToDeductForDebt.compareTo(BigDecimal.ZERO) > 0 ? " (Đã cấn trừ dư nợ)" : ""))
                        .build());
            }

            if (amountToDeductForDebt.compareTo(BigDecimal.ZERO) > 0) {
                transactions.add(Transaction.builder()
                        .wallet(wallet)
                        .booking(booking)
                        .amount(amountToDeductForDebt.negate())
                        .balanceAffected(BalanceAffected.DEBT)
                        .type(TransactionType.SYSTEM_FEE)
                        .status(TransactionStatus.SUCCESS)
                        .description("Tự động thanh toán dư nợ từ doanh thu đơn " + booking.getBookingCode())
                        .build());
            }

            transactionRepository.saveAll(transactions);

            log.info("Đã xử lý mở khóa đơn {}. Khả dụng: +{} VNĐ | Cấn trừ nợ: {} VNĐ",
                    booking.getBookingCode(), finalAvailableIncome, amountToDeductForDebt);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createPayoutRequest(Long hostId, PayoutCreateRequest request) {
        if (request.amountPayout().compareTo(BigDecimal.valueOf(5000)) < 0) {
            throw new InvalidDataException("Số tiền rút tối thiểu là 5,000 VNĐ.");
        }

        Wallet wallet = walletRepository.findByUser_Id(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví của Chủ nhà"));

        User host = wallet.getUser();

        BankAccount bankAccount = bankAccountRepository.findByIdAndUser_Id(request.bankAccountId(), hostId)
                .orElseThrow(() -> new InvalidDataException("Tài khoản ngân hàng không hợp lệ hoặc không thuộc sở hữu của bạn."));

        if (wallet.getDebtBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidDataException("Bạn đang có dư nợ " + wallet.getDebtBalance() + " VNĐ. Vui lòng thanh toán nợ trước khi rút tiền.");
        }

        if (wallet.getAvailableBalance().compareTo(request.amountPayout()) < 0) {
            throw new InvalidDataException("Số dư khả dụng không đủ để thực hiện lệnh rút này.");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(request.amountPayout()));
        walletRepository.save(wallet);

        Payout payout = Payout.builder()
                .wallet(wallet)
                .user(host)
                .amount(request.amountPayout())
                .bankCode(bankAccount.getBankCode())
                .accountNumber(bankAccount.getAccountNumber())
                .accountHolderName(bankAccount.getAccountHolderName())
                .status(PayoutStatus.REQUESTED)
                .build();
        payout = payoutRepository.save(payout);

        Transaction trans = Transaction.builder()
                .wallet(wallet)
                .payout(payout)
                .amount(request.amountPayout().negate())
                .balanceAffected(BalanceAffected.AVAILABLE)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .description("Yêu cầu rút tiền về " + bankAccount.getBankCode() + " (Đuôi " + bankAccount.getAccountNumber().substring(Math.max(0, bankAccount.getAccountNumber().length() - 4)) + ")")
                .build();
        transactionRepository.save(trans);

        log.info("Host {} vừa tạo lệnh rút {} VNĐ", hostId, request.amountPayout());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String processPayoutRequestByAdmin(Long payoutId, PayoutProcessRequest request) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lệnh rút tiền."));

        if (payout.getStatus() != PayoutStatus.REQUESTED) {
            throw new InvalidDataException("Chỉ có thể xử lý các lệnh rút tiền đang ở trạng thái REQUESTED.");
        }

        Transaction trans = transactionRepository.findByPayout_Id(payout.getId())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy giao dịch lịch sử của lệnh rút tiền này."));

        if (request.isApproved()) {
            if (request.bankTransactionRef().isBlank()){
                throw new InvalidDataException("Khi duyệt lệnh rút tiền, mã giao dịch ngân hàng không được để trống.");
            }

            payout.setStatus(PayoutStatus.COMPLETED);
            payout.setBankTransactionRef(request.bankTransactionRef());
            payout.setProofImageUrl(request.proofImageUrl());
            payout.setAdminNote(request.adminNote());
            payout.setProcessedAt(LocalDateTime.now());

            trans.setStatus(TransactionStatus.SUCCESS);
            log.info("Admin đã duyệt thành công lệnh rút tiền ID {}. Mã giao dịch NH: {}", payoutId, request.bankTransactionRef());

        } else {
            if (request.adminNote().isBlank()){
                throw new InvalidDataException("Khi từ chối lệnh rút tiền, ghi chú của admin không được để trống.");
            }
            payout.setStatus(PayoutStatus.REJECTED);
            payout.setAdminNote(request.adminNote());
            payout.setProcessedAt(LocalDateTime.now());

            trans.setStatus(TransactionStatus.FAILED);

            Wallet wallet = payout.getWallet();
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(payout.getAmount()));
            walletRepository.save(wallet);

            log.info("Admin đã TỪ CHỐI lệnh rút tiền ID {}. Hoàn lại {} VNĐ vào ví Khả dụng.", payoutId, payout.getAmount());
        }

        payoutRepository.save(payout);
        transactionRepository.save(trans);

        return request.isApproved() ? "Lệnh rút tiền đã được duyệt thành công." : "Lệnh rút tiền đã bị từ chối. Số tiền đã được hoàn lại vào ví khả dụng.";
    }
}
