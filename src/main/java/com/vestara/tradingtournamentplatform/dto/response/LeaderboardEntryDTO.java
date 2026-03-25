package com.vestara.tradingtournamentplatform.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class LeaderboardEntryDTO {
    private final Long id;
    private final int rankPosition;
    private final Long userId;
    private final String displayName;
    private final Long tournamentId;
    private final BigDecimal totalPortfolioValue;
    private final BigDecimal returnPercentage;
    private final BigDecimal totalPnl;
    private final int totalTrades;
    private final Instant lastCalculatedAt;
}