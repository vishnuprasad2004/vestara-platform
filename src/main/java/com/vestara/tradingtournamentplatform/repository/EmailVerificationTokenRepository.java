package com.vestara.tradingtournamentplatform.repository;

import com.vestara.tradingtournamentplatform.entity.EmailVerificationToken;
import com.vestara.tradingtournamentplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    // A user should only have one active verification token at a time
    // Delete old ones before issuing a new one
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user = :user")
    void deleteAllByUser(User user);

    // Cleanup job
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now OR t.used = true")
    void deleteExpiredAndUsed(@Param("now") Instant now);
}