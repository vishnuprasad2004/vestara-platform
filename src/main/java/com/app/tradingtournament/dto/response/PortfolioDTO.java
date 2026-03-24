package com.app.tradingtournament.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PortfolioDTO {
    private final Long id;
    private final Long userId;
    private final String displayName;
    private final Long tournamentId;
    private final String tournamentName;
    private final BigDecimal cashBalance;
    private final BigDecimal initialCapital;
    private final BigDecimal totalPortfolioValue;
    private final BigDecimal totalUnrealizedPnl;
    private final BigDecimal returnPercentage;
    private final List<HoldingDTO> holdings;
}