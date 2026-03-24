package com.app.tradingtournament.service;

import com.app.tradingtournament.dto.response.LeaderboardEntryDTO;
import com.app.tradingtournament.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaderboardService {

    // Triggered after every trade + by scheduler every 60s
    void recalculate(Long tournamentId);

    // Public — paginated leaderboard
    Page<LeaderboardEntryDTO> getLeaderboard(Long tournamentId, Pageable pageable);

    // Authenticated — my rank in a tournament
    LeaderboardEntryDTO getMyRank(Long tournamentId, UserPrincipal principal);
}