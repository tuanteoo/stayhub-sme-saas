package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingStatusScheduler {
    private final BookingRepository bookingRepository;
    private final WalletService walletService;

    @Scheduled(cron = "0 */30 * * * *")
    @SchedulerLock(
            name = "autoCheckoutTask",
            lockAtLeastFor = "5m",
            lockAtMostFor = "14m"
    )
    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteCheckedOutBookings() {
        log.info("Bắt đầu quét các đơn hàng CHECKED_OUT quá 24h...");

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

    @Scheduled(cron = "0 */15 * * * *")
    @SchedulerLock(
            name = "autoCheckoutTask",
            lockAtLeastFor = "5m",
            lockAtMostFor = "14m"
    )
    @Transactional(rollbackFor = Exception.class)
    public void autoCheckoutOverdueBookings() {
        log.info("[SCHEDULER] Bắt đầu quét các booking quá giờ trả phòng...");

        LocalDate today = LocalDate.now();
        List<Booking> checkedInBookings = bookingRepository.findByStatusAndCheckOutDateLessThanEqual(BookingStatus.CHECKED_IN, today);

        int processedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : checkedInBookings) {
            LocalDate checkOutDate = booking.getCheckOutDate();
            if (checkOutDate == null) continue;

            String checkoutBeforeStr = booking.getProperty().getCheckoutBefore();

            LocalTime checkoutTime = parseTime(checkoutBeforeStr, LocalTime.of(12, 0));

            LocalDateTime exactCheckoutDeadline = LocalDateTime.of(checkOutDate, checkoutTime);

            LocalDateTime overdueThreshold = exactCheckoutDeadline.plusMinutes(15);

            if (now.isAfter(overdueThreshold)) {
                booking.setStatus(BookingStatus.CHECKED_OUT);
                bookingRepository.save(booking);

                processedCount++;
                log.info("[SCHEDULER] Đã Auto-checkout thành công Booking Code: {}", booking.getBookingCode());
            }
        }

        if (processedCount > 0) {
            log.info("Hoàn tất quét. Đã xử lý auto-checkout cho {} booking.", processedCount);
        }

        log.info("[SCHEDULER] Hoàn tất quét. Đã xử lý auto-checkout cho {} booking.", processedCount);
    }

    private LocalTime parseTime(String timeStr, LocalTime defaultTime) {
        if (timeStr == null || timeStr.trim().isBlank()) {
            return defaultTime;
        }

        try {
            String cleanTimeStr = timeStr.trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
            return LocalTime.parse(cleanTimeStr, formatter);

        } catch (DateTimeParseException e) {
            log.warn("Định dạng giờ trả phòng không chuẩn ({}). Sử dụng giờ mặc định.", timeStr);
            return defaultTime;
        }
    }
}

