package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.response.LeaderboardEntryDTO;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
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