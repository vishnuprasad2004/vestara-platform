package com.vestara.tradingtournamentplatform.controller;

import com.vestara.tradingtournamentplatform.dto.ApiResponse;
import com.vestara.tradingtournamentplatform.dto.response.LeaderboardEntryDTO;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournaments/{tournamentId}/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Tournament leaderboard and rankings")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    @Operation(summary = "Get tournament leaderboard — public")
    public ResponseEntity<ApiResponse<Page<LeaderboardEntryDTO>>> getLeaderboard(
            @PathVariable Long tournamentId,
            @PageableDefault(size = 50, sort = "rankPosition",
                    direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                leaderboardService.getLeaderboard(tournamentId, pageable)
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('leaderboard:read')")
    @Operation(summary = "Get my rank in a tournament")
    public ResponseEntity<ApiResponse<LeaderboardEntryDTO>> getMyRank(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                leaderboardService.getMyRank(tournamentId, principal)
        ));
    }
}