package com.stayhub.backend.Module.Property.Service.Implement;

import com.stayhub.backend.Module.Property.Model.Room;
import com.stayhub.backend.Module.Property.Model.RoomAvailability;
import com.stayhub.backend.Module.Property.Repository.RoomAvailabilityRepository;
import com.stayhub.backend.Module.Property.Repository.RoomRepository;
import com.stayhub.backend.Module.Property.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
}
