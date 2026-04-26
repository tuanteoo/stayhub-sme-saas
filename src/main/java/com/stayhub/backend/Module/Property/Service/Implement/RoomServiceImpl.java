package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Module.Property.DTO.Request.CalendarUpdateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CalendarDayResponse;
import com.stayhub.backend.Module.Property.Model.Room;
import com.stayhub.backend.Module.Property.Model.RoomAvailability;
import com.stayhub.backend.Module.Property.Repository.RoomAvailabilityRepository;
import com.stayhub.backend.Module.Property.Repository.RoomRepository;
import com.stayhub.backend.Module.Property.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomRepository roomRepository;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshDailyAvailability() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(365);

        log.info("Bắt đầu chạy CRON Job cập nhật lịch phòng cho ngày {}...", today);

        roomAvailabilityRepository.lockPastDates(today);
        log.info("Đã khóa thành công lịch của các ngày trước {}", today);

        List<Room> activeRooms = roomRepository.findAllByIsActiveTrue();

        List<RoomAvailability> newAvailabilities = activeRooms.stream()
                .map(room -> RoomAvailability.builder()
                        .room(room)
                        .date(targetDate)
                        .isAvailable(true)
                        .priceModifier(BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        roomAvailabilityRepository.saveAll(newAvailabilities);
        log.info("Đã tạo lịch trống mới cho {} phòng vào ngày {}", activeRooms.size(), targetDate);
    }

    @Override
    public List<CalendarDayResponse> getRoomCalendar(Long hostId, Long roomId, int year, int month) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));

        if (!room.getProperty().getHost().getId().equals(hostId)) {
            throw new AuthorizationDeniedException("Bạn không có quyền xem lịch của phòng này.");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<RoomAvailability> availabilities = roomAvailabilityRepository
                .findByRoom_IdAndDateBetween(roomId, startDate, endDate);

        Map<LocalDate, List<RoomAvailability>> availabilityMap = availabilities.stream()
                .collect(Collectors.groupingBy(RoomAvailability::getDate));

        List<CalendarDayResponse> calendar = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<RoomAvailability> dayRecords = availabilityMap.getOrDefault(date, Collections.emptyList());

            boolean isSoldOut = dayRecords.stream().anyMatch(a -> a.getBooking() != null);

            Optional<RoomAvailability> manualConfig = dayRecords.stream()
                    .filter(a -> a.getBooking() == null)
                    .findFirst();

            BigDecimal customPrice = manualConfig.map(RoomAvailability::getPriceModifier).orElse(null);
            BigDecimal finalPrice = (customPrice != null && customPrice.compareTo(BigDecimal.ZERO) > 0)
                    ? customPrice
                    : room.getPricePerNight();

            boolean isLocked = manualConfig.map(a -> !a.getIsAvailable()).orElse(false);

            calendar.add(CalendarDayResponse.builder()
                    .date(date)
                    .price(finalPrice)
                    .isLocked(isLocked)
                    .isSoldOut(isSoldOut)
                    .build());
        }
        return calendar;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRoomCalendar(Long hostId, Long roomId, CalendarUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng"));

        if (!room.getProperty().getHost().getId().equals(hostId)) {
            throw new AuthorizationDeniedException("Bạn không có quyền cập nhật lịch của phòng này.");
        }

        List<LocalDate> targetDates = new ArrayList<>();
        boolean hasDatesList = request.dates() != null && !request.dates().isEmpty();
        boolean hasDateRange = request.startDate() != null && request.endDate() != null;

        if (hasDatesList && hasDateRange) {
            throw new InvalidDataException("Dữ liệu không hợp lệ: Vui lòng CHỈ truyền danh sách ngày rời rạc (dates) HOẶC khoảng thời gian (startDate, endDate). Tuyệt đối không truyền cả hai.");
        }

        if (hasDatesList) {
            targetDates.addAll(request.dates());
        } else if (hasDateRange) {
            if (request.startDate().isAfter(request.endDate())) {
                throw new InvalidDataException("Ngày bắt đầu không thể lớn hơn ngày kết thúc.");
            }
            for (LocalDate d = request.startDate(); !d.isAfter(request.endDate()); d = d.plusDays(1)) {
                targetDates.add(d);
            }
        } else {
            throw new InvalidDataException("Vui lòng cung cấp khoảng thời gian hoặc danh sách ngày cần cập nhật.");
        }

        if (targetDates.stream().anyMatch(d -> d.isBefore(LocalDate.now()))) {
            throw new InvalidDataException("Không thể cập nhật cấu hình lịch cho những ngày trong quá khứ.");
        }

        List<RoomAvailability> targets = roomAvailabilityRepository
                .findByRoom_IdAndDateInAndBookingIsNull(roomId, targetDates);

        Map<LocalDate, RoomAvailability> existingMap = targets.stream()
                .collect(Collectors.toMap(RoomAvailability::getDate, a -> a));

        List<RoomAvailability> toSave = new ArrayList<>();

        for (LocalDate date : targetDates) {
            RoomAvailability availability = existingMap.getOrDefault(date, new RoomAvailability());

            if (availability.getId() == null) {
                availability.setRoom(room);
                availability.setDate(date);
                availability.setBooking(null);
                availability.setIsAvailable(true);
                availability.setPriceModifier(BigDecimal.ZERO);
            }

            if (request.customPrice() != null) {
                availability.setPriceModifier(request.customPrice());
            }

            if (request.isLocked() != null) {
                availability.setIsAvailable(!request.isLocked());
            }

            toSave.add(availability);
        }

        roomAvailabilityRepository.saveAll(toSave);
    }
}
