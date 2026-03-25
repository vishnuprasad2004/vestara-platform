package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.response.PortfolioDTO;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;

public interface PortfolioService {

    // Get my portfolio in a tournament
    PortfolioDTO getMyPortfolio(Long tournamentId, UserPrincipal principal);

    // Get any participant's portfolio — admin/tournament owner only
    PortfolioDTO getPortfolioByUserId(Long tournamentId, Long userId);
}