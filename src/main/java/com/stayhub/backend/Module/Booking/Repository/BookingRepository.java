package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime time);
}
