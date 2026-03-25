package com.vestara.tradingtournamentplatform.dto.response;

import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentVisibility;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class TournamentDTO {
    private final Long id;
    private final String name;
    private final String description;
    private final TournamentStatus status;
    private final TournamentVisibility visibility;
    private final BigDecimal initialCapital;
    private final int maxParticipants;
    private final long currentParticipants;
    private final Instant startDate;
    private final Instant endDate;
    private final List<String> allowedSymbols;
    private final Long creatorId;
    private final String creatorName;
    private final Instant createdAt;
}