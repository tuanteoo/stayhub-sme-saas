package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Module.Booking.Model.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute,Long>, JpaSpecificationExecutor<Dispute> {
    boolean existsByBookingId(Long bookingId);

    @EntityGraph(attributePaths = {
            "booking",
            "booking.user",
            "booking.property.host",
            "creator",
            "creator.roles"
    })
    @Override
    @NonNull
    Page<Dispute> findAll(@Nullable Specification<Dispute> spec, @NonNull Pageable pageable);
}
