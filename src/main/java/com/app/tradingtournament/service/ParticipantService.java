package com.app.tradingtournament.service;

import com.app.tradingtournament.dto.request.DisqualifyParticipantRequest;
import com.app.tradingtournament.dto.response.ParticipantDTO;
import com.app.tradingtournament.entity.enums.ParticipantStatus;
import com.app.tradingtournament.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParticipantService {

    ParticipantDTO join(Long tournamentId, UserPrincipal principal);

    void withdraw(Long tournamentId, UserPrincipal principal);

    ParticipantDTO disqualify(
            Long tournamentId,
            Long userId,
            DisqualifyParticipantRequest request,
            UserPrincipal principal
    );

    Page<ParticipantDTO> getParticipants(
            Long tournamentId,
            ParticipantStatus status,
            Pageable pageable
    );

    ParticipantDTO getMyParticipation(Long tournamentId, UserPrincipal principal);
}