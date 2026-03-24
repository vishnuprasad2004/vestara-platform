package com.app.tradingtournament.repository;

import com.app.tradingtournament.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserIdAndTournamentId(Long userId, Long tournamentId);

    boolean existsByUserIdAndTournamentId(Long userId, Long tournamentId);

    // Leaderboard recalculation — all portfolios in a tournament
    List<Portfolio> findByTournamentId(Long tournamentId);

    // Stock price refresh scheduler — find all symbols currently
    // held across all active tournament portfolios
    // For scheduler — all symbols across all active tournament portfolios
    @Query("SELECT DISTINCT h.symbol FROM Holding h " +
            "WHERE h.quantity > 0 " +
            "AND h.portfolio.tournament.status = " +
            "com.app.tradingtournament.entity.enums.TournamentStatus.ACTIVE")
    List<String> findActiveSymbolsByTournamentId();
}