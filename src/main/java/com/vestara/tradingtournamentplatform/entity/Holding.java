package com.vestara.tradingtournamentplatform.entity;

import com.vestara.tradingtournamentplatform.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "holdings",
    indexes = {
        @Index(name = "idx_holdings_portfolio_id", columnList = "portfolio_id"),
        @Index(name = "idx_holdings_symbol", columnList = "symbol")
    },
    uniqueConstraints = {
        // One holding per symbol per portfolio
        @UniqueConstraint(
            name = "uk_holding_portfolio_symbol",
            columnNames = {"portfolio_id", "symbol"}
        )
    }
)
public class Holding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 10)
    private String symbol;

    // Current quantity owned — 0 means position closed, not deleted
    @Column(nullable = false)
    private int quantity;

    // Weighted average price paid across all buys
    // Used to calculate unrealized P&L
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal averageBuyPrice;

    // Optimistic locking — concurrent trades on same holding
    @Version
    private Long version;
}