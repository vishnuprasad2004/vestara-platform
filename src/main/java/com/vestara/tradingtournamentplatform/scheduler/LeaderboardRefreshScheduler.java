package com.vestara.tradingtournamentplatform.scheduler;

import com.vestara.tradingtournamentplatform.repository.LeaderboardEntryRepository;
import com.vestara.tradingtournamentplatform.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardRefreshScheduler {

    private final LeaderboardService leaderboardService;
    private final LeaderboardEntryRepository leaderboardEntryRepository;

    // Runs every 60 seconds
    @Scheduled(fixedDelay = 60_000)
    public void refreshLeaderboards() {
        // Find all tournament IDs that have leaderboard entries
        // (these are tournaments that have had at least one trade)
        List<Long> activeTournamentIds = leaderboardEntryRepository
                .findActiveTournamentIds();

        if (activeTournamentIds.isEmpty()) return;

        log.debug("Refreshing leaderboards for {} active tournaments",
                activeTournamentIds.size());

        for (Long tournamentId : activeTournamentIds) {
            try {
                leaderboardService.recalculate(tournamentId);
            } catch (Exception e) {
                // Never let one failure stop the rest from refreshing
                log.error("Leaderboard refresh failed for tournamentId={}: {}",
                        tournamentId, e.getMessage());
            }
        }
    }
}