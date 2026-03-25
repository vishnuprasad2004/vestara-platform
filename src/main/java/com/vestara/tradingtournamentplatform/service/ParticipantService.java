package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.request.DisqualifyParticipantRequest;
import com.vestara.tradingtournamentplatform.dto.response.ParticipantDTO;
import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
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