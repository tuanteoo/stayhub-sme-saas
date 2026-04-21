package com.stayhub.backend.Module.Booking.Repository;

import com.stayhub.backend.Common.Util.DisputeStatus;
import com.stayhub.backend.Module.Booking.Model.Dispute;
import org.springframework.data.jpa.domain.Specification;

public class DisputeSpecification {
    public static Specification<Dispute> hasStatus(DisputeStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Dispute> searchByBookingCode(String bookingCode) {
        return (root, query, criteriaBuilder) -> {
            if (bookingCode == null || bookingCode.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("booking").get("bookingCode")),
                    "%" + bookingCode.toLowerCase() + "%"
            );
        };
    }
}
