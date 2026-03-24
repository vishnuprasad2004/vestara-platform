package com.app.tradingtournament.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PlatformAnalyticsDTO {
    private final long totalUsers;
    private final long totalTournaments;
    private final long activeTournaments;
    private final long totalTrades;
    private final List<Map<String, Object>> mostTradedSymbols;
}