package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Common.Util.UserStatus;
import com.stayhub.backend.Module.Identity.Model.User;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<User> isNotAdmin() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var subRoot = subquery.from(User.class);
            var roleJoin = subRoot.join("roles");

            subquery.select(subRoot.get("id"))
                    .where(criteriaBuilder.equal(roleJoin.get("name"), "ROLE_ADMIN"));

            return criteriaBuilder.not(root.get("id").in(subquery));
        };
    }

    public static Specification<User> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("profile").get("fullName")), likePattern)
            );
        };
    }
}
