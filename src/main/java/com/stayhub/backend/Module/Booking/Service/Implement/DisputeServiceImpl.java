package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Common.Util.DisputeStatus;
import com.stayhub.backend.Common.Util.PaginationUtil;
import com.stayhub.backend.Module.Booking.DTO.Request.DisputeCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.DisputeAdminResponse;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Model.Dispute;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Booking.Repository.DisputeRepository;
import com.stayhub.backend.Module.Booking.Repository.DisputeSpecification;
import com.stayhub.backend.Module.Booking.Service.DisputeService;
import com.stayhub.backend.Module.Identity.Model.Role;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

        if (booking.getStatus() == BookingStatus.DISPUTED || disputeRepository.existsByBookingId(booking.getId())) {
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

    @Override
    public PageResponse<DisputeAdminResponse> getDisputesForAdmin(String status, int pageNo, int pageSize, String sortBy, String sortDir) {
        Pageable pageable = PaginationUtil.getPageable(pageNo, pageSize, sortBy, sortDir, "createdAt");

        List<Specification<Dispute>> specs = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            try {
                DisputeStatus disputeStatus = DisputeStatus.valueOf(status.toUpperCase());
                specs.add(DisputeSpecification.hasStatus(disputeStatus));
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Trạng thái khiếu nại không hợp lệ.");
            }
        }

        Specification<Dispute> finalSpec = Specification.allOf(specs);
        Page<Dispute> disputePage = disputeRepository.findAll(finalSpec, pageable);

        List<DisputeAdminResponse> responses = disputePage.getContent().stream()
                .map(d -> {
                    String currentDisputeRole = "UNKNOWN";
                    Long creatorId = d.getCreator().getId();

                    if (creatorId.equals(d.getBooking().getUser().getId())) {
                        currentDisputeRole = "USER";
                    } else if (creatorId.equals(d.getBooking().getProperty().getHost().getId())) {
                        currentDisputeRole = "HOST";
                    }

                    return DisputeAdminResponse.builder()
                            .id(d.getId())
                            .bookingCode(d.getBooking().getBookingCode())
                            .creatorId(creatorId)
                            .creatorName(d.getCreator().getProfile().getFullName())
                            .creatorEmail(d.getCreator().getEmail())
                            .creatorRole(currentDisputeRole)
                            .reason(d.getReason())
                            .evidenceImageUrls(d.getEvidenceImageUrls())
                            .status(d.getStatus().name())
                            .createdAt(d.getCreatedAt())
                            .build();
                }).toList();

        return PageResponse.<DisputeAdminResponse>builder()
                .pageNo(disputePage.getNumber() + 1)
                .pageSize(disputePage.getSize())
                .totalPage(disputePage.getTotalPages())
                .totalElements(disputePage.getTotalElements())
                .items(responses)
                .build();
    }
}
