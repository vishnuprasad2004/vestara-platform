package com.app.tradingtournament.scheduler;

import com.app.tradingtournament.repository.EmailVerificationTokenRepository;
import com.app.tradingtournament.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    // Runs every 24 hours
    @Scheduled(fixedDelay = 86_400_000)
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        emailVerificationTokenRepository.deleteExpiredAndUsed(Instant.now());
        log.info("Token cleanup completed");
    }
}