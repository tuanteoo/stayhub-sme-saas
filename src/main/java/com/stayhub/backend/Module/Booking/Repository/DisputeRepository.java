package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Common.Util.DisputeStatus;
import com.stayhub.backend.Module.Booking.Model.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute,Long> {
    boolean existsByBookingId(Long bookingId);

    Optional<Dispute> findByBookingId(Long bookingId);

    Page<Dispute> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    Page<Dispute> findByStatusOrderByCreatedAtDesc(DisputeStatus status, Pageable pageable);
}
