package com.stayhub.backend.Module.Identity.Repository;

import com.stayhub.backend.Module.Identity.Model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
