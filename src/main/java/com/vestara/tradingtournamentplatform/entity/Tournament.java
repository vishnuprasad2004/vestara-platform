package com.vestara.tradingtournamentplatform.entity;

import com.vestara.tradingtournamentplatform.entity.base.BaseEntity;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "tournaments",
    indexes = {
        @Index(name = "idx_tournaments_status", columnList = "status"),
        @Index(name = "idx_tournaments_created_by", columnList = "created_by_user_id")
    }
)
@SQLDelete(sql = "UPDATE tournaments SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Tournament extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TournamentStatus status;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    // Virtual capital each participant starts with
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal initialCapital;

    // 0 = unlimited
    @Column(nullable = false)
    private int maxParticipants;

    // Soft delete
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // Tournament creator — the TOURNAMENT_ADMIN
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User creator;

    // Owned symbols — cascade all operations, delete orphans
    @OneToMany(
        mappedBy = "tournament",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<TournamentAllowedSymbol> allowedSymbols = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TournamentVisibility visibility;
}