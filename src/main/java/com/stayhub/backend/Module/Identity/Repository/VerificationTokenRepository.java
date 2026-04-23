package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Common.Util.VerificationType;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Identity.Model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, VerificationType type);
    Optional<VerificationToken> findByUserAndType(User user, VerificationType type);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken v WHERE v.user = :user AND v.type = :type")
    void deleteByUserAndType(User user, VerificationType type);
}
