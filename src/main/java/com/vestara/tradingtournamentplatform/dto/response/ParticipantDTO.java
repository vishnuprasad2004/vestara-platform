
package com.vestara.tradingtournamentplatform.dto.response;

import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ParticipantDTO {
    private final Long id;
    private final Long userId;
    private final String displayName;
    private final String email;
    private final Long tournamentId;
    private final String tournamentName;
    private final ParticipantStatus status;
    private final String statusReason;
    private final Instant joinedAt;
}