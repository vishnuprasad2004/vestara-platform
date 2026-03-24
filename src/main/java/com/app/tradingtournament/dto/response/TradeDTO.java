package com.app.tradingtournament.dto.response;

import com.app.tradingtournament.entity.enums.TradeStatus;
import com.app.tradingtournament.entity.enums.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class TradeDTO {
    private final Long id;
    private final Long userId;
    private final Long tournamentId;
    private final String symbol;
    private final TradeType tradeType;
    private final TradeStatus status;
    private final int quantity;
    private final BigDecimal pricePerShare;
    private final BigDecimal totalValue;
    private final String idempotencyKey;
    private final Instant executedAt;
}