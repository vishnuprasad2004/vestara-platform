package com.app.tradingtournament.service;

import com.app.tradingtournament.dto.request.CancelTournamentRequest;
import com.app.tradingtournament.dto.request.CreateTournamentRequest;
import com.app.tradingtournament.dto.request.UpdateTournamentRequest;
import com.app.tradingtournament.dto.response.TournamentDTO;
import com.app.tradingtournament.entity.enums.TournamentStatus;
import com.app.tradingtournament.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TournamentService {

    /**
     * Creates a new tournament in DRAFT status.
     */
    TournamentDTO create(CreateTournamentRequest request, UserPrincipal principal);

    /**
     * Retrieves tournament details by ID.
     */
    TournamentDTO getById(Long id);

    /**
     * Retrieves all non-deleted tournaments with pagination.
     */
    Page<TournamentDTO> getAll(Pageable pageable);

    /**
     * Filters tournaments by their current status.
     */
    Page<TournamentDTO> getByStatus(TournamentStatus status, Pageable pageable);

    /**
     * Retrieves tournaments created by the currently authenticated user.
     */
    Page<TournamentDTO> getMyTournaments(UserPrincipal principal, Pageable pageable);

    /**
     * Updates tournament details. Only allowed if the tournament is in DRAFT status.
     */
    TournamentDTO update(Long id, UpdateTournamentRequest request, UserPrincipal principal);

    /**
     * Moves a tournament from DRAFT to REGISTRATION_OPEN.
     */
    TournamentDTO publish(Long id, UserPrincipal principal);

    /**
     * Cancels an ongoing or upcoming tournament.
     */
    TournamentDTO cancel(Long id, CancelTournamentRequest request, UserPrincipal principal);

    /**
     * Performs a soft delete on a tournament. Only allowed for DRAFT status.
     */
    void delete(Long id, UserPrincipal principal);
}