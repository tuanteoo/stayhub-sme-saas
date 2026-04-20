package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Common.Util.DisputeStatus;
import com.stayhub.backend.Module.Booking.Model.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute,Long> {
    boolean existsByBookingId(Long bookingId);

    @EntityGraph(attributePaths = {"booking", "creator", "creator.roles"})
    @Query("SELECT d FROM Dispute d")
    Page<Dispute> findAllWithGraph(Pageable pageable);

    @EntityGraph(attributePaths = {"booking", "creator", "creator.roles"})
    @Query("SELECT d FROM Dispute d WHERE d.status = :status")
    Page<Dispute> findByStatusWithGraph(DisputeStatus status, Pageable pageable);
}
