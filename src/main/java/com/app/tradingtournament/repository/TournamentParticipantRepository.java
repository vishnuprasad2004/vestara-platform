package com.app.tradingtournament.repository;

import com.app.tradingtournament.entity.TournamentParticipant;
import com.app.tradingtournament.entity.enums.ParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    Optional<TournamentParticipant> findByUserIdAndTournamentId(
        Long userId,
        Long tournamentId
    );

    boolean existsByUserIdAndTournamentId(Long userId, Long tournamentId);

    // All participants in a tournament — paginated for admin view
    Page<TournamentParticipant> findByTournamentId(Long tournamentId, Pageable pageable);

    // Active participants only — used by leaderboard and trade validation
    List<TournamentParticipant> findByTournamentIdAndStatus(
        Long tournamentId,
        ParticipantStatus status
    );

    Page<TournamentParticipant> findByTournamentIdAndStatus(
            Long tournamentId,
            ParticipantStatus status,
            Pageable pageable
    );


    // Count active participants — used for max participants check
    long countByTournamentIdAndStatus(Long tournamentId, ParticipantStatus status);

    // All tournaments a user has joined
    Page<TournamentParticipant> findByUserId(Long userId, Pageable pageable);
}