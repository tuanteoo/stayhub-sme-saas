package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Module.Booking.Model.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {
}
