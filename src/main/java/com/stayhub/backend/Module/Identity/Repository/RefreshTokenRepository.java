package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Module.Identity.Model.RefreshToken;
import com.stayhub.backend.Module.Identity.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.token = :token")
    void deleteByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user AND r.deviceInfo = :deviceInfo")
    void deleteOldTokens(User user, String deviceInfo);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < CURRENT_TIMESTAMP")
    int deleteExpiredTokens();
}
