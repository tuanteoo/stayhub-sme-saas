package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.*;
import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.DTO.Response.GuestBookingResponse;
import com.stayhub.backend.Module.Booking.DTO.Response.HostBookingResponse;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Model.BookingRoom;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Booking.Service.BookingService;
import com.stayhub.backend.Module.Finance.Model.Payment;
import com.stayhub.backend.Module.Finance.Repository.PaymentRepository;
import com.stayhub.backend.Module.Finance.Service.PaymentService;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.Model.*;
import com.stayhub.backend.Module.Property.Repository.PropertyRepository;
import com.stayhub.backend.Module.Property.Repository.RoomAvailabilityRepository;
import com.stayhub.backend.Module.Property.Repository.RoomRepository;
import com.stayhub.backend.Module.Property.Repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;

    @Lazy
    @Autowired
    private PaymentService paymentService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createBooking(BookingCreateRequest request, Long id) {
        User guest = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng trong hệ thống"));

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỗ ở trong hệ thống"));

        if (property.getHost().getId().equals(guest.getId())) {
            throw new InvalidDataException("Hành vi không hợp lệ: Bạn không thể tự đặt chỗ ở của chính mình!");
        }

        if (!PropertyStatus.ACTIVE.equals(property.getStatus())){
            throw new InvalidDataException("Bài đăng này chưa được duyệt hoặc không hoạt động. Vui lòng chọn chỗ ở khác!");
        }

        long totalNights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        if (totalNights <= 0) {
            throw new InvalidDataException("Ngày trả phòng phải diễn ra sau ngày nhận phòng");
        }

        List<Long> requestedRoomIds = request.roomIds();
        List<Room> rooms = roomRepository.findAllById(requestedRoomIds);

        if (rooms.size() != requestedRoomIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều phòng được chọn không tồn tại trong hệ thống!");
        }

        boolean allRoomsBelongToProperty = rooms.stream()
                .allMatch(room -> room.getProperty().getId().equals(property.getId()));
        if (!allRoomsBelongToProperty) {
            throw new InvalidDataException("Dữ liệu không hợp lệ: Các phòng được chọn không thuộc về chỗ ở này!");
        }

        String rentalTypeSlug = property.getRentalType().getSlug();
        if ("toan-bo-nha".equals(rentalTypeSlug)) {
            long totalActiveRooms = property.getRooms().stream().filter(Room::getIsActive).count();
            if (requestedRoomIds.size() != totalActiveRooms) {
                throw new InvalidDataException("Bài đăng này cho thuê toàn bộ chỗ ở. Vui lòng chọn tất cả các phòng!");
            }
        }

        // =========================================================================================
        // BƯỚC 2: KHÓA DATABASE (PESSIMISTIC LOCK) ĐỂ CHỐNG DOUBLE-BOOKING
        // =========================================================================================
        List<RoomAvailability> availabilities = roomAvailabilityRepository
                .findAndLockAvailabilities(requestedRoomIds, request.checkInDate(), request.checkOutDate());

        // Kiểm tra xem phòng đã được sinh lịch đủ chưa
        long expectedDays = totalNights * requestedRoomIds.size();
        if (availabilities.size() != expectedDays) {
            throw new InvalidDataException("Chỗ ở chưa được Host thiết lập lịch cho khoảng thời gian này!");
        }

        boolean isAnyDayBooked = availabilities.stream().anyMatch(a -> !Boolean.TRUE.equals(a.getIsAvailable()));
        if (isAnyDayBooked) {
            throw new InvalidDataException("Rất tiếc, phòng vừa được khách khác đặt nhanh tay hơn. Vui lòng chọn ngày khác!");
        }

        // =========================================================================================
        // BƯỚC 3: KIỂM TRA TỔNG SỨC CHỨA VÀ TÍNH TOÁN TIỀN PHÒNG
        // =========================================================================================
        int totalCapacity = rooms.stream().mapToInt(Room::getMaxGuests).sum();
        if (request.totalGuests() > totalCapacity) {
            throw new InvalidDataException("Tổng số khách (" + request.totalGuests() + " người) vượt quá sức chứa tối đa của các phòng đã chọn (" + totalCapacity + " người).");
        }

        BigDecimal totalRoomPrice = BigDecimal.ZERO;
        List<BookingRoom> bookingRooms = new ArrayList<>();

        int weekendSurcharge = property.getWeekendSurchargePercentage() != null ? property.getWeekendSurchargePercentage() : 0;
        BigDecimal surchargeMultiplier = BigDecimal.valueOf(100 + weekendSurcharge).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        for (RoomAvailability availability : availabilities) {
            Room room = availability.getRoom();
            LocalDate date = availability.getDate();

            BigDecimal dailyPrice;
            BigDecimal customPrice = availability.getPriceModifier();

            if (customPrice != null && customPrice.compareTo(BigDecimal.ZERO) > 0) {
                dailyPrice = customPrice;
            }
            else {
                BigDecimal basePrice = room.getPricePerNight();
                if (date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    dailyPrice = basePrice.multiply(surchargeMultiplier);
                } else {
                    dailyPrice = basePrice;
                }
            }

            totalRoomPrice = totalRoomPrice.add(dailyPrice);
        }

        for (Room room : rooms) {
            bookingRooms.add(BookingRoom.builder()
                    .room(room)
                    .priceAtBooking(room.getPricePerNight())
                    .build());
        }

        BigDecimal cleaningFee = property.getCleaningFee() != null ? property.getCleaningFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        BigDecimal finalAmount = totalRoomPrice.add(cleaningFee).subtract(discountAmount);

        // =========================================================================================
        // BƯỚC 4: TÍNH TOÁN SỐ TIỀN ĐẶT CỌC / THANH TOÁN (LUÔN > 0)
        // =========================================================================================
        BigDecimal depositAmount;
        BigDecimal remainingAmount;
        boolean isFullyPaid = false;
        int appliedDepositPercentage = 100;

        if (request.paymentOption() == BookingPaymentOption.PAY_AT_CHECKIN) {
            if (!Boolean.TRUE.equals(property.getIsPayAtCheckinAllowed())) {
                throw new InvalidDataException("Chỗ ở này không cho phép thanh toán tại chỗ. Vui lòng chọn thanh toán toàn bộ.");
            }

            Integer configPercentage = property.getDepositPercentage();
            appliedDepositPercentage = configPercentage != null ? configPercentage : 100;

            BigDecimal depositRatio = BigDecimal.valueOf(appliedDepositPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            depositAmount = finalAmount.multiply(depositRatio).setScale(0, RoundingMode.HALF_UP);
            remainingAmount = finalAmount.subtract(depositAmount);
        } else {
            depositAmount = finalAmount;
            remainingAmount = BigDecimal.ZERO;
        }

        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Số tiền cần thanh toán không hợp lệ (Phải lớn hơn 0đ)!");
        } else if (depositAmount.compareTo(finalAmount) == 0) {
            isFullyPaid = true;
        }

        UserSubscription hostSubscription = userSubscriptionRepository.findFirstByUser_IdAndStatusOrderByStartDateDesc(
                property.getHost().getId(),
                UserSubscriptionStatus.ACTIVE
        ).orElseThrow(() -> new InvalidDataException("Chủ nhà hiện không có gói đăng ký hợp lệ, không thể nhận đơn đặt phòng."));

        BigDecimal rawPercentage = hostSubscription.getCurrentCommissionRate();
        if (rawPercentage == null) {
            throw new ResourceNotFoundException("Lỗi hệ thống: Không tìm thấy tỷ lệ hoa hồng cho chủ nhà.");
        }
        BigDecimal commissionRate = rawPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal platformCommissionAmount = finalAmount.multiply(commissionRate).setScale(0, RoundingMode.HALF_UP);


        // =========================================================================================
        // BƯỚC 5: LƯU ĐƠN ĐẶT PHÒNG VÀO DATABASE
        // =========================================================================================
        String bookingCode = "SHB-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(guest)
                .property(property)
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .totalNights((int) totalNights)
                .totalGuests(request.totalGuests())

                .totalPrice(totalRoomPrice)
                .cleaningFee(cleaningFee)
                .discountAmount(discountAmount)

                .paymentOption(request.paymentOption())
                .depositPercentage(appliedDepositPercentage)
                .depositAmount(depositAmount)
                .remainingAmount(remainingAmount)
                .platformCommission(platformCommissionAmount)
                .cancellationPolicy(property.getCancellationPolicy())

                .status(BookingStatus.AWAITING_PAYMENT)
                .isFullyPaid(isFullyPaid)
                .note(request.note())
                .build();

        for (BookingRoom br : bookingRooms) {
            br.setBooking(booking);
        }
        booking.getBookingRooms().addAll(bookingRooms);

        bookingRepository.save(booking);

        for (RoomAvailability a : availabilities) {
            a.setIsAvailable(false);
            a.setBooking(booking);
        }
        roomAvailabilityRepository.saveAll(availabilities);

        PaymentPurpose purpose = PaymentPurpose.BOOKING_PAYMENT;

        String uniqueTxnRef = booking.getBookingCode() + "-" + System.currentTimeMillis() + "-";

        Payment payment = Payment.builder()
                .booking(booking)
                .user(guest)
                .amount(booking.getDepositAmount())
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentPurpose(purpose)
                .transactionRef(uniqueTxnRef)
                .build();

        paymentRepository.save(payment);

        return booking.getBookingCode();
    }

    @Override
    public PageResponse<HostBookingResponse> getBookingsForHost(Long hostId, int page, int size) {
        Pageable pageable = PaginationUtil.getPageable(page, size, "createdAt", "desc");

        Page<Booking> bookingPage = bookingRepository.findBookingsByHostId(hostId, pageable);

        List<HostBookingResponse> hostBookingResponses = bookingPage.stream()
                .map(this::mapToHostBookingResponse)
                .toList();

        return new PageResponse<>(
                bookingPage.getNumber() + 1,
                bookingPage.getSize(),
                bookingPage.getTotalPages(),
                bookingPage.getTotalElements(),
                hostBookingResponses
        );
    }

    @Override
    public PageResponse<GuestBookingResponse> getBookingForGuest(Long guestId, int page, int size) {
        Pageable pageable = PaginationUtil.getPageable(page, size, "createdAt", "desc");

        Page<Booking> bookingPage = bookingRepository.findByUser_IdOrderByCreatedAtDesc(guestId, pageable);

        List<GuestBookingResponse> bookingResponses = bookingPage.stream()
                .map(this::mapToGuestBookingResponse)
                .toList();

        return new PageResponse<>(
                bookingPage.getNumber() + 1,
                bookingPage.getSize(),
                bookingPage.getTotalPages(),
                bookingPage.getTotalElements(),
                bookingResponses
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void releaseBookingInternal(Booking booking, BookingStatus targetStatus, Long cancelledBy) {
        booking.setStatus(targetStatus);

        if (targetStatus == BookingStatus.CANCELLED || targetStatus == BookingStatus.EXPIRED || targetStatus == BookingStatus.REJECTED) {
            booking.setCancelledAt(LocalDateTime.now());
            booking.setCancelledBy(cancelledBy);
        }

        bookingRepository.save(booking);

        List<RoomAvailability> availabilities = roomAvailabilityRepository.findByBooking_Id(booking.getId());

        for (RoomAvailability availability : availabilities) {
            availability.setIsAvailable(true);
            availability.setBooking(null);
        }

        roomAvailabilityRepository.saveAll(availabilities);

        log.info("Đã chuyển đơn {} sang trạng thái {} và giải phóng {} ngày phòng. Người tác động: {}",
                booking.getBookingCode(), targetStatus, availabilities.size(), cancelledBy == null ? "SYSTEM" : cancelledBy);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String cancelBookingByGuest(Long guestId, String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt phòng."));

        if (!booking.getUser().getId().equals(guestId)) {
            throw new InvalidDataException("Bạn không có quyền thao tác trên đơn hàng này.");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED
                && booking.getStatus() != BookingStatus.PARTIALLY_PAID
                && booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            throw new InvalidDataException("Chỉ có thể hủy đơn hàng đang chờ thanh toán, đã cọc hoặc đã xác nhận.");
        }

        if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
            releaseBookingInternal(booking, BookingStatus.CANCELLED, guestId);

            paymentRepository.findFirstByBooking_IdAndPaymentStatusOrderByCreatedAtDesc(booking.getId(), PaymentStatus.PENDING)
                    .ifPresent(p -> {
                        p.setPaymentStatus(PaymentStatus.CANCELLED);
                        paymentRepository.save(p);
                    });

            return "Hủy đơn hàng thành công.";
        }

        Payment originalPayment = paymentRepository.findFirstByBooking_IdAndPaymentStatusOrderByCreatedAtDesc(
                        booking.getId(), PaymentStatus.COMPLETED)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy lịch sử thanh toán thành công cho đơn hàng này."));

        BigDecimal refundAmount = calculateRefundAmount(booking);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            boolean isRefunded = paymentService.refundVnPayTransaction(originalPayment, refundAmount);

            if (!isRefunded) {
                throw new AppException(ErrorCode.PAYMENT_FAILED);
            }

            Payment refundPayment = Payment.builder()
                    .booking(booking)
                    .user(booking.getUser())
                    .amount(refundAmount)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .paymentStatus(PaymentStatus.REFUNDED)
                    .paymentPurpose(PaymentPurpose.REFUND)
                    .transactionRef("REF-" + booking.getBookingCode())
                    .gatewayTransactionNo(originalPayment.getGatewayTransactionNo())
                    .build();
            paymentRepository.save(refundPayment);
        }

        releaseBookingInternal(booking, BookingStatus.CANCELLED, guestId);

        return refundAmount.compareTo(BigDecimal.ZERO) > 0
                ? "Hủy thành công. Số tiền " + refundAmount + " VNĐ đã được gửi yêu cầu hoàn trả qua VNPAY."
                : "Hủy thành công. Bạn không được hoàn tiền do vi phạm chính sách hủy.";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String hostCheckIn(Long hostId, String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        Long actualHostId = booking.getBookingRooms().get(0).getRoom().getProperty().getHost().getId();
        if (!actualHostId.equals(hostId)) throw new InvalidDataException("Bạn không có quyền thao tác đơn này.");

        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckInDate()) || today.isAfter(booking.getCheckOutDate())) {
            throw new InvalidDataException("Chỉ có thể Check-in vào ngày nhận phòng hoặc trong khoảng thời gian lưu trú.");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PARTIALLY_PAID) {
            throw new InvalidDataException("Chỉ có thể Check-in cho đơn hàng đã thanh toán hoặc đã xác nhận.");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
        return "Xác nhận Check-in thành công. Khách hàng không thể hủy đơn này nữa.";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String hostCheckOut(Long hostId, String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        Long actualHostId = booking.getBookingRooms().get(0).getRoom().getProperty().getHost().getId();
        if (!actualHostId.equals(hostId)) throw new InvalidDataException("Bạn không có quyền thao tác đơn này.");

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new InvalidDataException("Chỉ có thể Check-out khi đơn hàng đang ở trạng thái Check-in.");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
        return "Check-out thành công. Đang chờ khách xác nhận hoàn thành chuyến đi.";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String guestCompleteBooking(Long guestId, String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!booking.getUser().getId().equals(guestId)) throw new AuthorizationDeniedException("Bạn không có quyền thao tác đơn này.");

        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new InvalidDataException("Chỉ có thể hoàn thành khi Chủ nhà đã Check-out.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        walletService.unlockPendingBalance(booking);
        return "Tuyệt vời! Chuyến đi đã hoàn tất. Cảm ơn bạn đã sử dụng StayHub.";
    }

    private HostBookingResponse mapToHostBookingResponse(Booking booking) {

        BigDecimal total = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal cleaning = booking.getCleaningFee() != null ? booking.getCleaningFee() : BigDecimal.ZERO;
        BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = total.add(cleaning).subtract(discount);

        BigDecimal amountPaid = Boolean.TRUE.equals(booking.getIsFullyPaid())
                ? finalAmount
                : (booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO);

        return HostBookingResponse.builder()
                .bookingCode(booking.getBookingCode())
                .guestName(booking.getUser().getProfile().getFullName())
                .propertyName(booking.getProperty().getName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalGuests(booking.getTotalGuests())
                .finalAmount(finalAmount)
                .amountPaid(amountPaid)
                .isFullyPaid(booking.getIsFullyPaid())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
    private GuestBookingResponse mapToGuestBookingResponse(Booking booking) {
        String thumbnail = booking.getProperty().getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .map(PropertyImage::getUrl)
                .findFirst()
                .orElse(booking.getProperty().getImages().isEmpty() ? null : booking.getProperty().getImages().get(0).getUrl());

        BigDecimal roomPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal cleaningFee = booking.getCleaningFee() != null ? booking.getCleaningFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = roomPrice.add(cleaningFee);

        return GuestBookingResponse.builder()
                .bookingCode(booking.getBookingCode())
                .propertyName(booking.getProperty().getName())
                .propertyAddress(booking.getProperty().getAddressDetail() + ", " + booking.getProperty().getProvince())
                .hostName(booking.getProperty().getHost().getProfile().getFullName())
                .hostEmail(booking.getProperty().getHost().getHostDetail().getSupportEmail())
                .hostPhone(booking.getProperty().getHost().getHostDetail().getBusinessPhone())

                .thumbnailUrl(thumbnail)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalAmount(finalTotal)
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
    private BigDecimal calculateRefundAmount(Booking booking) {
        LocalDate today = LocalDate.now();
        CancellationPolicy policy = booking.getCancellationPolicy();
        LocalDate deadline = booking.getCheckInDate().minusDays(policy.getDaysBeforeCheckin());

        BigDecimal amountPaid = booking.getDepositAmount();
        BigDecimal totalOrder = booking.getDepositAmount().add(booking.getRemainingAmount());

        if (today.isBefore(deadline)) {
            return amountPaid;
        } else {
            BigDecimal penaltyRate = BigDecimal.valueOf(100 - policy.getRefundPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal penaltyAmount = totalOrder.multiply(penaltyRate);

            BigDecimal refund = amountPaid.subtract(penaltyAmount);
            return refund.compareTo(BigDecimal.ZERO) > 0 ? refund : BigDecimal.ZERO;
        }
    }
}
