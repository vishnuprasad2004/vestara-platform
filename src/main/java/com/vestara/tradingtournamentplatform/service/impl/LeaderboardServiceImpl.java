
package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.dto.response.LeaderboardEntryDTO;
import com.vestara.tradingtournamentplatform.entity.LeaderboardEntry;
import com.vestara.tradingtournamentplatform.entity.Portfolio;
import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import com.vestara.tradingtournamentplatform.exception.ResourceNotFoundException;
import com.vestara.tradingtournamentplatform.repository.LeaderboardEntryRepository;
import com.vestara.tradingtournamentplatform.repository.PortfolioRepository;
import com.vestara.tradingtournamentplatform.repository.TournamentParticipantRepository;
import com.vestara.tradingtournamentplatform.repository.TradeRepository;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.LeaderboardService;
import com.vestara.tradingtournamentplatform.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final PortfolioRepository portfolioRepository;
    private final TournamentParticipantRepository participantRepository;
    private final TradeRepository tradeRepository;
    private final MarketDataService marketDataService;

    // ── Recalculate ───────────────────────────────────────────────

    @Override
    @Transactional
    public void recalculate(Long tournamentId) {
        log.debug("Recalculating leaderboard for tournamentId: {}", tournamentId);

        // Fetch all active participants
        List<Portfolio> portfolios = portfolioRepository
                .findByTournamentId(tournamentId);

        if (portfolios.isEmpty()) return;

        // Build ranked entries
        List<LeaderboardEntry> entries = new ArrayList<>();

        for (Portfolio portfolio : portfolios) {
            // Skip disqualified participants
            boolean isActive = participantRepository
                    .findByUserIdAndTournamentId(
                            portfolio.getUser().getId(), tournamentId)
                    .map(p -> p.getStatus() == ParticipantStatus.ACTIVE)
                    .orElse(false);

            if (!isActive) continue;

            // Calculate total portfolio value
            BigDecimal marketValue = portfolio.getHoldings()
                    .stream()
                    .filter(h -> h.getQuantity() > 0)
                    .map(h -> marketDataService.getPrice(h.getSymbol())
                            .multiply(BigDecimal.valueOf(h.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalValue = portfolio.getCashBalance().add(marketValue);

            // Return %
            BigDecimal returnPct = BigDecimal.ZERO;
            if (portfolio.getInitialCapital().compareTo(BigDecimal.ZERO) > 0) {
                returnPct = totalValue
                        .subtract(portfolio.getInitialCapital())
                        .divide(portfolio.getInitialCapital(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            // Total P&L
            BigDecimal totalPnl = totalValue.subtract(portfolio.getInitialCapital());

            // Total trades
            int totalTrades = (int) tradeRepository.countByUserIdAndTournamentId(
                    portfolio.getUser().getId(), tournamentId
            );

            LeaderboardEntry entry = LeaderboardEntry.builder()
                    .user(portfolio.getUser())
                    .tournament(portfolio.getTournament())
                    .totalPortfolioValue(totalValue)
                    .returnPercentage(returnPct)
                    .totalPnl(totalPnl)
                    .totalTrades(totalTrades)
                    .rankPosition(0)    // assigned after sorting
                    .lastCalculatedAt(Instant.now())
                    .build();

            entries.add(entry);
        }

        // Sort — totalPortfolioValue DESC → returnPercentage DESC → totalTrades ASC
        entries.sort(
            Comparator.comparing(LeaderboardEntry::getTotalPortfolioValue)
                .reversed()
                .thenComparing(Comparator.comparing(
                    LeaderboardEntry::getReturnPercentage).reversed())
                .thenComparingInt(LeaderboardEntry::getTotalTrades)
        );

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRankPosition(i + 1);
        }

        // Delete existing entries and replace with fresh batch
        leaderboardEntryRepository.deleteAllByTournamentId(tournamentId);
        leaderboardEntryRepository.saveAll(entries);

        log.debug("Leaderboard recalculated for tournamentId: {} — {} entries",
                tournamentId, entries.size());
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryDTO> getLeaderboard(
            Long tournamentId,
            Pageable pageable
    ) {
        return leaderboardEntryRepository
                .findByTournamentIdOrderByRankPositionAsc(tournamentId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaderboardEntryDTO getMyRank(
            Long tournamentId,
            UserPrincipal principal
    ) {
        return leaderboardEntryRepository
                .findByUserIdAndTournamentId(principal.getId(), tournamentId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.LEADERBOARD_NOT_FOUND
                ));
    }

    // ── Private helpers ───────────────────────────────────────────

    private LeaderboardEntryDTO mapToDTO(LeaderboardEntry entry) {
        return LeaderboardEntryDTO.builder()
                .id(entry.getId())
                .rankPosition(entry.getRankPosition())
                .userId(entry.getUser().getId())
                .displayName(entry.getUser().getDisplayName())
                .tournamentId(entry.getTournament().getId())
                .totalPortfolioValue(entry.getTotalPortfolioValue())
                .returnPercentage(entry.getReturnPercentage())
                .totalPnl(entry.getTotalPnl())
                .totalTrades(entry.getTotalTrades())
                .lastCalculatedAt(entry.getLastCalculatedAt())
                .build();
    }
}