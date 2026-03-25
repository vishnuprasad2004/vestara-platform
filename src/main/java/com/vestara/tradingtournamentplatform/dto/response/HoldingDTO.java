package com.vestara.tradingtournamentplatform.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class HoldingDTO {
    private final Long id;
    private final String symbol;
    private final int quantity;
    private final BigDecimal averageBuyPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal marketValue;
    private final BigDecimal unrealizedPnl;
    private final BigDecimal unrealizedPnlPercent;
}