package com.stayhub.backend.Module.Identity.Service;

import com.stayhub.backend.Module.Identity.Repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting expired refresh tokens cleanup job at {}", LocalDateTime.now());
        int deletedCount = refreshTokenRepository.deleteExpiredTokens();
        log.info("Finished cleanup expired refresh tokens. Total deleted: {}", deletedCount);
    }
}
