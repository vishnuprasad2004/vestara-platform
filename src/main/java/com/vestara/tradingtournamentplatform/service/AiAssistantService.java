package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.response.AiAssistantResponse;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;

public interface AiAssistantService {
    AiAssistantResponse chat(Long tournamentId, String message, UserPrincipal principal);
}