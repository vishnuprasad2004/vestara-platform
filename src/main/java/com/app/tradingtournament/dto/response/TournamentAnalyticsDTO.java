package com.app.tradingtournament.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TournamentAnalyticsDTO {
    private final Long tournamentId;
    private final String tournamentName;
    private final long totalParticipants;
    private final long totalTrades;
    private final List<Map<String, Object>> mostTradedSymbols;
    private final String topPerformerName;
    private final BigDecimal topPerformerReturn;
    private final BigDecimal averagePortfolioValue;
}