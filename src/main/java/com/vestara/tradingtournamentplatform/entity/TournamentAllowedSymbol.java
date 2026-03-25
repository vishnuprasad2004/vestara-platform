package com.vestara.tradingtournamentplatform.entity;

import com.vestara.tradingtournamentplatform.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "tournament_allowed_symbols",
    indexes = {
        @Index(name = "idx_allowed_symbols_tournament_id", columnList = "tournament_id")
    },
    uniqueConstraints = {
        // Same symbol cannot appear twice in the same tournament
        @UniqueConstraint(
            name = "uk_tournament_symbol",
            columnNames = {"tournament_id", "symbol"}
        )
    }
)
public class TournamentAllowedSymbol extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    // Stock ticker e.g. AAPL, TSLA, MSFT
    @Column(nullable = false, length = 10)
    private String symbol;
}