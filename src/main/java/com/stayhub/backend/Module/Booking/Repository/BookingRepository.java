package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Module.Booking.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
}
