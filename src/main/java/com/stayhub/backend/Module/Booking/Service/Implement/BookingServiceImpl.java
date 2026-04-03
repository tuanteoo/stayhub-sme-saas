package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BookingPaymentOption;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Common.Util.PricingUtils;
import com.stayhub.backend.Module.Booking.DTO.Request.BookingCreateRequest;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Model.BookingRoom;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Booking.Service.BookingService;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Repository.UserRepository;
import com.stayhub.backend.Module.Property.Model.Property;
import com.stayhub.backend.Module.Property.Model.Room;
import com.stayhub.backend.Module.Property.Model.RoomAvailability;
import com.stayhub.backend.Module.Property.Repository.PropertyRepository;
import com.stayhub.backend.Module.Property.Repository.RoomAvailabilityRepository;
import com.stayhub.backend.Module.Property.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createBooking(BookingCreateRequest request, Long id) {
        // BƯỚC 1: LẤY THÔNG TIN VÀ KIỂM TRA NGHIỆP VỤ CƠ BẢN
        // =========================================================================================
        User guest = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỗ ở"));

        long totalNights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        if (totalNights <= 0) {
            throw new InvalidDataException("Ngày trả phòng phải diễn ra sau ngày nhận phòng");
        }

        List<Long> requestedRoomIds = request.roomIds();

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

        // Kiểm tra xem có ngày nào bị khóa bởi đơn đặt phòng khác chưa
        boolean isAnyDayBooked = availabilities.stream().anyMatch(a -> !a.getIsAvailable());
        if (isAnyDayBooked) {
            throw new InvalidDataException("Rất tiếc, phòng vừa được khách khác đặt nhanh tay hơn. Vui lòng chọn ngày khác!");
        }

        // =========================================================================================
        // BƯỚC 3: KIỂM TRA TỔNG SỨC CHỨA VÀ TÍNH TOÁN TIỀN PHÒNG
        // =========================================================================================
        List<Room> rooms = roomRepository.findAllById(requestedRoomIds);

        // KIỂM TRA SỨC CHỨA
        int totalCapacity = rooms.stream().mapToInt(Room::getMaxGuests).sum();
        if (request.totalGuests() > totalCapacity) {
            throw new InvalidDataException("Tổng số khách (" + request.totalGuests() + " người) vượt quá sức chứa tối đa của các phòng đã chọn (" + totalCapacity + " người).");
        }

        BigDecimal totalRoomPrice = BigDecimal.ZERO;
        List<BookingRoom> bookingRooms = new ArrayList<>();

        int weekendSurcharge = property.getWeekendSurchargePercentage() != null ? property.getWeekendSurchargePercentage() : 0;
        BigDecimal surchargeMultiplier = BigDecimal.valueOf(100 + weekendSurcharge).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Map<Long, List<RoomAvailability>> availabilityByRoomId = availabilities.stream()
                .collect(Collectors.groupingBy(a -> a.getRoom().getId()));

        for (Room room : rooms) {
            BigDecimal roomTotalPrice = BigDecimal.ZERO;

            List<RoomAvailability> roomAvailabilities = availabilityByRoomId.getOrDefault(room.getId(), new ArrayList<>());
            for (RoomAvailability availability : roomAvailabilities) {
                BigDecimal dailyPrice = PricingUtils.calculateDailyPrice(room, availability, surchargeMultiplier);

                roomTotalPrice = roomTotalPrice.add(dailyPrice);
            }

            totalRoomPrice = totalRoomPrice.add(roomTotalPrice);

            bookingRooms.add(BookingRoom.builder()
                    .room(room)
                    .numGuests(room.getMaxGuests())
                    .priceAtBooking(roomTotalPrice)
                    .build());
        }

        BigDecimal cleaningFee = property.getCleaningFee() != null ? property.getCleaningFee() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // TỔNG TIỀN KHÁCH PHẢI TRẢ (Tiền phòng + Dọn dẹp - Giảm giá)
        BigDecimal finalAmount = totalRoomPrice.add(cleaningFee).subtract(discountAmount);

        // =========================================================================================
        // BƯỚC 4: TÍNH TOÁN SỐ TIỀN ĐẶT CỌC (DEPOSIT)
        // =========================================================================================
        BigDecimal depositAmount = finalAmount;
        BigDecimal remainingAmount = BigDecimal.ZERO;

        if (request.paymentOption() == BookingPaymentOption.PAY_AT_CHECKIN) {
            if (!property.getIsPayAtCheckinAllowed()) {
                throw new InvalidDataException("Chỗ ở này không cho phép thanh toán tại chỗ");
            }
            int depositPercentage = property.getDepositPercentage() != null ? property.getDepositPercentage() : 0;
            depositAmount = finalAmount.multiply(BigDecimal.valueOf(depositPercentage)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            remainingAmount = finalAmount.subtract(depositAmount);
        }

        // =========================================================================================
        // BƯỚC 5: LƯU ĐƠN ĐẶT PHÒNG VÀO DATABASE
        // =========================================================================================
        // Sinh mã đơn (Ví dụ: BKG-8A9B2C)
        String bookingCode = "SHB-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();

        BookingStatus initialStatus = (depositAmount.compareTo(BigDecimal.ZERO) == 0)
                ? BookingStatus.CONFIRMED
                : BookingStatus.AWAITING_PAYMENT;

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
                .depositPercentage(request.paymentOption() == BookingPaymentOption.PAY_IN_FULL ? 100 : property.getDepositPercentage())
                .depositAmount(depositAmount)
                .remainingAmount(remainingAmount)
                .status(initialStatus)
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
        return booking.getBookingCode();
    }
}
