package com.app.tradingtournament.repository;

import com.app.tradingtournament.entity.Trade;
import com.app.tradingtournament.entity.enums.TradeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByUserIdAndTournamentId(
        Long userId,
        Long tournamentId,
        Pageable pageable
    );

    Page<Trade> findByTournamentId(Long tournamentId, Pageable pageable);

    // Idempotency check — before executing a trade
    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Trade> findByIdempotencyKey(String idempotencyKey);

    // Analytics — count trades per type in a tournament
    long countByTournamentIdAndTradeType(Long tournamentId, TradeType tradeType);

    // Total trades by a user in a tournament — used for leaderboard tie-breaking
    long countByUserIdAndTournamentId(Long userId, Long tournamentId);

    // Most traded symbols in a tournament — for analytics
    @Query("SELECT t.symbol AS symbol, COUNT(t) AS tradeCount " +
            "FROM Trade t GROUP BY t.symbol ORDER BY tradeCount DESC")
    List<Map<String, Object>> findMostTradedSymbols(Pageable pageable);
}