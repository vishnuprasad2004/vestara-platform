package com.vestara.tradingtournamentplatform.entity;

import com.vestara.tradingtournamentplatform.entity.base.BaseEntity;
import com.vestara.tradingtournamentplatform.entity.enums.TradeStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TradeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "trades",
    indexes = {
        @Index(name = "idx_trades_user_id", columnList = "user_id"),
        @Index(name = "idx_trades_tournament_id", columnList = "tournament_id"),
        @Index(name = "idx_trades_symbol", columnList = "symbol"),
        @Index(name = "idx_trades_executed_at", columnList = "executedAt")
    }
)
public class Trade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tournament tournament;

    @Column(nullable = false, length = 10, updatable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4, updatable = false)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Column(nullable = false, updatable = false)
    private int quantity;

    // Price at the exact moment of execution
    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal pricePerShare;

    // quantity × pricePerShare — stored for fast reporting
    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal totalValue;

    @Column(nullable = false, updatable = false)
    private Instant executedAt;

    // Idempotency key stored to detect duplicate submissions
    @Column(unique = true, updatable = false, length = 36)
    private String idempotencyKey;
}