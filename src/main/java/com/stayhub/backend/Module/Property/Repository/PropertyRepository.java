package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Module.Property.Model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property,Long>, JpaSpecificationExecutor<Property> {
    long countByHostIdAndStatusNotIn(Long hostId, Collection<PropertyStatus> statuses);
    Page<Property> findByHostId(Long hostId, Pageable pageable);
    Page<Property> findByCategory_SlugAndStatus(String categorySlug, PropertyStatus status, Pageable pageable);
    Optional<Property> findBySlugAndStatus(String slug, PropertyStatus status);
    Optional<Property> findFirstByHostIdAndStatusOrderByCreatedAtAsc(Long id, PropertyStatus status);

    @Query("""
        SELECT DISTINCT p FROM Property p
        JOIN p.rooms r
        WHERE p.status = 'PUBLISHED'
        AND (:province IS NULL OR p.province = :province)
        AND (:district IS NULL OR p.district = :district)
        AND r.maxGuests >= :numGuests
        AND NOT EXISTS (
            SELECT 1 FROM RoomAvailability ra
            WHERE ra.room.id = r.id
            AND ra.date >= :checkInDate 
            AND ra.date < :checkOutDate 
            AND ra.isAvailable = false
        )
    """)
    Page<Property> searchAvailableProperties(
            @Param("province") String province,
            @Param("district") String district,
            @Param("numGuests") Integer numGuests,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            Pageable pageable
    );

    Optional<Property> findFirstByHostIdOrderByCreatedAtAsc(Long hostId);

    @EntityGraph(attributePaths = {"host", "host.profile", "host.hostDetail", "category"})
    @Query("SELECT p FROM Property p " +
            "JOIN p.host u " +
            "JOIN u.hostDetail hd " +
            "WHERE hd.onboardingStatus = :onboardingStatus")
    Page<Property> findAllByOnboardingStatus(
            @Param("onboardingStatus") HostOnboardingStatus onboardingStatus,
            Pageable pageable);

    @EntityGraph(attributePaths = {"host", "host.profile", "host.hostDetail", "category"})
    @Query("SELECT p FROM Property p " +
            "JOIN p.host u " +
            "JOIN u.hostDetail hd " +
            "WHERE hd.onboardingStatus = :onboardingStatus " +
            "AND p.status = :status")
    Page<Property> findAllByOnboardingStatusAndPropertyStatus(
            @Param("onboardingStatus") HostOnboardingStatus onboardingStatus,
            @Param("status") PropertyStatus status,
            Pageable pageable);

    @Query("SELECT p.status, COUNT(p) FROM Property p WHERE p.host.id = :hostId GROUP BY p.status")
    List<Object[]> countPropertiesGroupedByStatus(@Param("hostId") Long hostId);
}
