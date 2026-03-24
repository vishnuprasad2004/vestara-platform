package com.app.tradingtournament.entity;

import com.app.tradingtournament.entity.base.BaseEntity;
import com.app.tradingtournament.entity.enums.ParticipantStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "tournament_participants",
    indexes = {
        @Index(name = "idx_participants_tournament_id", columnList = "tournament_id"),
        @Index(name = "idx_participants_user_id", columnList = "user_id")
    },
    uniqueConstraints = {
        // A user can only join a tournament once
        @UniqueConstraint(
            name = "uk_participant_user_tournament",
            columnNames = {"user_id", "tournament_id"}
        )
    }
)
public class TournamentParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tournament tournament;

    @Column(nullable = false)
    private Instant joinedAt;

    @Column(nullable = false)
    private boolean disqualified = false;

    // Reason populated only when disqualified = true
    @Column(columnDefinition = "TEXT")
    private String disqualificationReason;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ParticipantStatus status;

    // Populated when status = DISQUALIFIED or WITHDRAWN
    @Column(columnDefinition = "TEXT")
    private String statusReason;

}