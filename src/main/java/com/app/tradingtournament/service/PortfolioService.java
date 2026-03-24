package com.app.tradingtournament.service;

import com.app.tradingtournament.dto.response.PortfolioDTO;
import com.app.tradingtournament.security.UserPrincipal;

public interface PortfolioService {

    // Get my portfolio in a tournament
    PortfolioDTO getMyPortfolio(Long tournamentId, UserPrincipal principal);

    // Get any participant's portfolio — admin/tournament owner only
    PortfolioDTO getPortfolioByUserId(Long tournamentId, Long userId);
}