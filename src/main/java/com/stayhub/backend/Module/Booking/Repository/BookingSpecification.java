package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import org.springframework.data.jpa.domain.Specification;

public class BookingSpecification {
    public static Specification<Booking> hasHostId(Long hostId) {
        return (root, query, criteriaBuilder) -> {
            var roomJoin = root.join("bookingRooms").join("room");
            var propertyJoin = roomJoin.join("property");
            var hostJoin = propertyJoin.join("host");
            return criteriaBuilder.equal(hostJoin.get("id"), hostId);
        };
    }

    public static Specification<Booking> hasGuestId(Long guestId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), guestId);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
