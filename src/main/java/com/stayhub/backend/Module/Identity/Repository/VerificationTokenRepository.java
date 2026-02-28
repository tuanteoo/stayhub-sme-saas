package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Common.Util.VerificationType;
import com.stayhub.backend.Module.Identity.Model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, VerificationType type);
}
