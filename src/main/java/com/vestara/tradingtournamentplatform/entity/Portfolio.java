package com.vestara.tradingtournamentplatform.entity;

import com.vestara.tradingtournamentplatform.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "portfolios",
    indexes = {
        @Index(name = "idx_portfolios_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolios_tournament_id", columnList = "tournament_id")
    },
    uniqueConstraints = {
        // One portfolio per user per tournament
        @UniqueConstraint(
            name = "uk_portfolio_user_tournament",
            columnNames = {"user_id", "tournament_id"}
        )
    }
)
public class Portfolio extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tournament tournament;

    // Current available cash — decreases on BUY, increases on SELL
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    // Never changes after creation — used to calculate return %
    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal initialCapital;

    // Optimistic locking — prevents concurrent trade race conditions
    // See DECISIONS.md #012
    @Version
    private Long version;

    @OneToMany(
        mappedBy = "portfolio",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Holding> holdings = new ArrayList<>();
}