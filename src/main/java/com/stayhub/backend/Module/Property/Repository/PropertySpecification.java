package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Common.Util.StringUtil;
import com.stayhub.backend.Module.Property.Model.Category;
import com.stayhub.backend.Module.Property.Model.Property;
import com.stayhub.backend.Module.Property.Model.Room;
import com.stayhub.backend.Module.Property.Model.RoomAvailability;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

    public static Specification<Property> buildSearchFilter(
            String destination,
            Integer guestCount,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String categorySlug) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), PropertyStatus.ACTIVE));

            if (destination != null && !destination.trim().isEmpty()) {
                String normalizedKeyword = StringUtil.normalizeForSearch(destination);
                String searchPattern = "%" + normalizedKeyword + "%";
                predicates.add(criteriaBuilder.like(root.get("searchText"), searchPattern));
            }

            Join<Property, Room> roomJoin = root.join("rooms");

            if (guestCount != null && guestCount > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(roomJoin.get("maxGuests"), guestCount));
            }

            if (checkInDate != null && checkOutDate != null) {
                Subquery<Integer> availabilitySubquery = query.subquery(Integer.class);
                Root<RoomAvailability> availabilityRoot = availabilitySubquery.from(RoomAvailability.class);
                availabilitySubquery.select(criteriaBuilder.literal(1));

                Predicate isSameRoom = criteriaBuilder.equal(availabilityRoot.get("room"), roomJoin);
                Predicate isDateInRange = criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(availabilityRoot.get("date"), checkInDate),
                        criteriaBuilder.lessThan(availabilityRoot.get("date"), checkOutDate)
                );
                Predicate isNotAvailable = criteriaBuilder.equal(availabilityRoot.get("isAvailable"), false);

                availabilitySubquery.where(isSameRoom, isDateInRange, isNotAvailable);

                predicates.add(criteriaBuilder.not(criteriaBuilder.exists(availabilitySubquery)));
            }

            if (categorySlug != null && !categorySlug.trim().isEmpty()) {
                Join<Property, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("slug"), categorySlug));
            }


            if (query != null) {
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Property> hasHostId(Long hostId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("host").get("id"), hostId);
    }

    public static Specification<Property> hasStatus(PropertyStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Property> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String normalizedKeyword = StringUtil.normalizeForSearch(keyword);
            String likePattern = "%" + normalizedKeyword + "%";

            return criteriaBuilder.like(root.get("searchText"), likePattern);
        };
    }
}
