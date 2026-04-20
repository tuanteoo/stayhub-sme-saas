package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Common.Util.UserStatus;
import com.stayhub.backend.Module.Identity.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"profile", "roles", "hostDetail"})
    @Query("SELECT u FROM User u WHERE (:status IS NULL OR u.status = :status) " +
            "AND NOT EXISTS (SELECT r FROM u.roles r WHERE r.name = 'ROLE_ADMIN')")
    Page<User> findAllUsersForAdmin(@Param("status") UserStatus status, Pageable pageable);
}
