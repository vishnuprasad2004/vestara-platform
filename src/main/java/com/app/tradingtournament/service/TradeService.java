package com.app.tradingtournament.service;

import com.app.tradingtournament.dto.request.TradeRequest;
import com.app.tradingtournament.dto.response.TradeDTO;
import com.app.tradingtournament.security.UserPrincipal;
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