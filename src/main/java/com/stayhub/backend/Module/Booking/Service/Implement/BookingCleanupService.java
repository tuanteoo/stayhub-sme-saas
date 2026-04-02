package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Property.Repository.RoomAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingCleanupService {
    private final BookingRepository bookingRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);

        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.AWAITING_PAYMENT, expireTime);

        if (!expiredBookings.isEmpty()) {
            log.info("Tìm thấy {} đơn đặt phòng quá 15 phút chưa thanh toán...", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.EXPIRED);
                booking.setCancellationReason("Hết hạn thanh toán (Quá 15 phút không hoàn tất)");
                bookingRepository.save(booking);

                roomAvailabilityRepository.releaseRoomsByBooking(booking);

                log.info("Đã đánh dấu hết hạn đơn hàng: {}", booking.getBookingCode());
            }
        }
    }
}
