package com.stayhub.backend.Module.Property.Repository;

import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Common.Util.StringUtil;
import com.stayhub.backend.Module.Property.Model.Property;
import jakarta.persistence.criteria.Predicate;
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
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxGuests"), guestCount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
