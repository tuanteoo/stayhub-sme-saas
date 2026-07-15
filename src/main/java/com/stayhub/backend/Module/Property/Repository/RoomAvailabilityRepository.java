package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Property.Model.RoomAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    @Modifying
    @Query("UPDATE RoomAvailability r SET r.isAvailable = false WHERE r.date < :today AND r.isAvailable = true")
    void lockPastDates(@Param("today") LocalDate today);

    List<RoomAvailability> findByBooking_Id(Long bookingId);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.room.id IN :roomIds AND ra.date >= :checkInDate AND ra.date < :checkOutDate ORDER BY ra.id ASC")
    List<RoomAvailability> findAvailabilities(
            @Param("roomIds") List<Long> roomIds,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Modifying
    @Query("UPDATE RoomAvailability r SET r.isAvailable = true, r.booking = null WHERE r.booking = :booking")
    void releaseRoomsByBooking(@Param("booking") Booking booking);

    List<RoomAvailability> findByRoom_IdAndDateBetween(Long roomId, LocalDate startDate, LocalDate endDate);
    List<RoomAvailability> findByRoom_IdAndDateIn(Long roomId, List<LocalDate> dates);
}
