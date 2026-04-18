package com.stayhub.backend.Module.Booking.Service;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingStatusScheduler {
    private final BookingRepository bookingRepository;
    private final WalletService walletService;

    // Chạy mỗi giờ 1 lần
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteCheckedOutBookings() {
        log.info("⏳ Bắt đầu quét các đơn hàng CHECKED_OUT quá 24h...");

        LocalDateTime deadline = LocalDateTime.now().minusHours(24);

        List<Booking> bookingsToComplete = bookingRepository.findByStatusAndUpdatedAtBefore(
                BookingStatus.CHECKED_OUT, deadline);

        if (bookingsToComplete.isEmpty()) {
            log.info("Không có đơn hàng nào cần tự động hoàn thành.");
            return;
        }

        for (Booking booking : bookingsToComplete) {
            try {
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRepository.save(booking);

                walletService.unlockPendingBalance(booking);
                log.info("Đã tự động đổi trạng thái và cộng tiền Khả dụng cho đơn hàng {}", booking.getBookingCode());
            } catch (Exception e) {
                log.error("Lỗi tự động hoàn thành đơn {}: {}", booking.getBookingCode(), e.getMessage());
            }
        }
    }
}
