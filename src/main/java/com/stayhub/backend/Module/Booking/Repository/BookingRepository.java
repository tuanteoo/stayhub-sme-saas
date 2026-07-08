package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long>, JpaSpecificationExecutor<Booking> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime time);
    Page<Booking> findByUser_IdOrderByCreatedAtDesc(Long guestId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.property.host.id = :hostId ORDER BY b.createdAt DESC")
    Page<Booking> findBookingsByHostId(@Param("hostId") Long hostId, Pageable pageable);

    List<Booking> findByStatusAndUpdatedAtBefore(BookingStatus status, LocalDateTime time);
    List<Booking> findByStatus(BookingStatus status);
}
