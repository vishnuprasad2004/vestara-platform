package com.app.tradingtournament.entity;

import com.app.tradingtournament.entity.base.BaseEntity;
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
        name = "leaderboard_entries",
        indexes = {
                @Index(name = "idx_leaderboard_tournament_id", columnList = "tournament_id"),
                @Index(name = "idx_leaderboard_rank", columnList = "rankPosition")
        },
        uniqueConstraints = {
                // One leaderboard entry per user per tournament
                @UniqueConstraint(
                        name = "uk_leaderboard_user_tournament",
                        columnNames = {"user_id", "tournament_id"}
                )
        }
)
public class LeaderboardEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tournament tournament;

    // cash + market value of all holdings at last calculation
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPortfolioValue;

    // ((totalPortfolioValue - initialCapital) / initialCapital) × 100
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal returnPercentage;

    // Realized + unrealized profit/loss
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPnl;

    // Total number of trades executed
    @Column(nullable = false)
    private int totalTrades;

    // Rank in the tournament — 1 = best
    // Tie-break: totalPortfolioValue DESC → returnPercentage DESC → totalTrades ASC
    @Column(nullable = false)
    private int rankPosition;

    // When this entry was last recalculated
    @Column(nullable = false)
    private Instant lastCalculatedAt;
}