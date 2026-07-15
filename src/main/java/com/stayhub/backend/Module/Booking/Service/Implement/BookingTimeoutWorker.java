package com.stayhub.backend.Module.Booking.Service.Implement;

import com.rabbitmq.client.Channel;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Config.RabbitMQConfig;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Booking.Service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingTimeoutWorker {
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @RabbitListener(queues = RabbitMQConfig.TIMEOUT_QUEUE)
    public void processExpiredBooking(String bookingCode, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("RabbitMQ: Nhận đơn hàng cần kiểm tra - {}", bookingCode);

            bookingRepository.findByBookingCode(bookingCode).ifPresent(booking -> {
                if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
                    booking.setCancellationReason("Hủy tự động: Quá 15 phút không hoàn tất thanh toán.");
                    bookingService.releaseBookingInternal(booking, BookingStatus.EXPIRED, null);
                    log.info("Đã hủy và nhả phòng cho đơn {}", bookingCode);
                }
            });

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi xử lý đơn hàng {}: {}", bookingCode, e.getMessage());
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
