package com.vestara.tradingtournamentplatform.repository;

import com.vestara.tradingtournamentplatform.entity.LeaderboardEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    // Primary leaderboard query — paginated, pre-sorted by rank
    Page<LeaderboardEntry> findByTournamentIdOrderByRankPositionAsc(
            Long tournamentId,
            Pageable pageable
    );

    Optional<LeaderboardEntry> findByUserIdAndTournamentId(
            Long userId,
            Long tournamentId
    );

    // Leaderboard recalculation fetches all entries for batch upsert
    List<LeaderboardEntry> findByTournamentId(Long tournamentId);

    // Used by scheduler to recalculate all active tournament leaderboards
    @Query("""
        SELECT DISTINCT le.tournament.id
        FROM LeaderboardEntry le
        WHERE le.tournament.status = 'ACTIVE'
    """)
    List<Long> findActiveTournamentIds();

    // Batch delete before recalculation — cleaner than individual upserts
    @Modifying
    @Query("DELETE FROM LeaderboardEntry le WHERE le.tournament.id = :tournamentId")
    void deleteAllByTournamentId(Long tournamentId);
}