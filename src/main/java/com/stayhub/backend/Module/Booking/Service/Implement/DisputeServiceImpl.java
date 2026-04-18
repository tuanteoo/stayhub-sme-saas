package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Common.Util.DisputeStatus;
import com.stayhub.backend.Module.Booking.DTO.Request.DisputeCreateRequest;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Model.Dispute;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Booking.Repository.DisputeRepository;
import com.stayhub.backend.Module.Booking.Service.DisputeService;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisputeServiceImpl implements DisputeService {
    private final BookingRepository bookingRepository;
    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createDispute(Long userId,String bookingCode,DisputeCreateRequest request) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User"));

        if (booking.getStatus() == BookingStatus.DISPUTED || disputeRepository.existsByBookingId(userId)) {
            throw new InvalidDataException("Đơn hàng này đang trong quá trình tranh chấp và xử lý.");
        }

        boolean isGuest = booking.getUser().getId().equals(userId);
        boolean isHost = booking.getBookingRooms().get(0).getRoom().getProperty().getHost().getId().equals(userId);

        if (!isGuest && !isHost) {
            throw new InvalidDataException("Bạn không có quyền khiếu nại đơn hàng này.");
        }

        if (isHost) {
            if (booking.getStatus() != BookingStatus.CHECKED_IN && booking.getStatus() != BookingStatus.CHECKED_OUT) {
                throw new InvalidDataException("Chủ nhà chỉ có thể khiếu nại khi khách đang lưu trú hoặc vừa Check-out (trước khi đơn hoàn tất).");
            }
        }

        if (isGuest) {
            if (booking.getStatus() != BookingStatus.CONFIRMED &&
                    booking.getStatus() != BookingStatus.CHECKED_IN &&
                    booking.getStatus() != BookingStatus.CHECKED_OUT) {
                throw new InvalidDataException("Khách hàng chỉ có thể khiếu nại từ lúc nhận phòng đến trước khi đơn hoàn tất.");
            }
        }

        Dispute newDispute = Dispute.builder()
                .booking(booking)
                .creator(user)
                .reason(request.reason())
                .description(request.description())
                .evidenceImageUrls(request.evidenceImageUrls())
                .status(DisputeStatus.OPEN)
                .build();
        disputeRepository.save(newDispute);

        log.info("Creating dispute for booking {} by user {}", booking.getBookingCode(), user.getId());

        booking.setStatus(BookingStatus.DISPUTED);
        bookingRepository.save(booking);

        return "Tạo khiếu nại thành công. Khiếu nại đang được Admin xem xét.";
    }
}
