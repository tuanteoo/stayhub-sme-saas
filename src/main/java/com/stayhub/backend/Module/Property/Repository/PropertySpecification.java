package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Common.Util.StringUtil;
import com.stayhub.backend.Module.Property.Model.Property;
import com.stayhub.backend.Module.Property.Model.Room;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {
    public static Specification<Property> buildSearchFilter(
            String destination,
            Integer guestCount) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), PropertyStatus.PUBLISHED));

            if (destination != null && !destination.trim().isEmpty()) {
                String normalizedKeyword = StringUtil.normalizeForSearch(destination);
                String searchPattern = "%" + normalizedKeyword + "%";
                predicates.add(criteriaBuilder.like(root.get("searchText"), searchPattern));
            }

            if (guestCount != null && guestCount > 0) {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<Room> roomRoot = subquery.from(Room.class);

                subquery.select(criteriaBuilder.sum(roomRoot.get("maxGuests")));
                subquery.where(criteriaBuilder.equal(roomRoot.get("property"), root));

                predicates.add(criteriaBuilder.greaterThanOrEqualTo(subquery, guestCount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
