package com.stayhub.backend.Module.Review.Repository;

import com.stayhub.backend.Module.Review.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBooking_Id(Long bookingId);
    Optional<Review> findByBooking_IdIn(List<Long> bookingIds);
    List<Review> findByProperty_IdAndIsVisibleTrueOrderByCreatedAtDesc(Long propertyId);
}
