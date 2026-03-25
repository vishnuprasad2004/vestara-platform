package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.request.TradeRequest;
import com.vestara.tradingtournamentplatform.dto.response.TradeDTO;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeService {

    TradeDTO buy(TradeRequest request, String idempotencyKey, UserPrincipal principal);

    TradeDTO sell(TradeRequest request, String idempotencyKey, UserPrincipal principal);

    Page<TradeDTO> getMyTrades(
            Long tournamentId,
            UserPrincipal principal,
            Pageable pageable
    );

    Page<TradeDTO> getAllTrades(Long tournamentId, Pageable pageable);
}