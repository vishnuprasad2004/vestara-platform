package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.dto.request.CancelTournamentRequest;
import com.vestara.tradingtournamentplatform.dto.request.CreateTournamentRequest;
import com.vestara.tradingtournamentplatform.dto.request.UpdateTournamentRequest;
import com.vestara.tradingtournamentplatform.dto.response.TournamentDTO;
import com.vestara.tradingtournamentplatform.entity.Tournament;
import com.vestara.tradingtournamentplatform.entity.TournamentAllowedSymbol;
import com.vestara.tradingtournamentplatform.entity.User;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentVisibility;
import com.vestara.tradingtournamentplatform.exception.ResourceNotFoundException;
import com.vestara.tradingtournamentplatform.exception.TournamentException;
import com.vestara.tradingtournamentplatform.repository.TournamentParticipantRepository;
import com.vestara.tradingtournamentplatform.repository.TournamentRepository;
import com.vestara.tradingtournamentplatform.repository.UserRepository;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.TournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final UserRepository userRepository;

    // ── Create ────────────────────────────────────────────────────

    @Transactional
    public TournamentDTO create(
            CreateTournamentRequest request,
            UserPrincipal principal
    ) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new TournamentException(
                    TournamentException.Code.INVALID_DATE_RANGE
            );
        }

        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.USER_NOT_FOUND,
                        principal.getId()
                ));

        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .description(request.getDescription())
                .initialCapital(request.getInitialCapital())
                .maxParticipants(request.getMaxParticipants())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TournamentStatus.DRAFT)
                .visibility(TournamentVisibility.PUBLIC)
                .creator(creator)
                .isDeleted(false)
                .build();

        // Wire allowed symbols if provided
        if (request.getAllowedSymbols() != null
                && !request.getAllowedSymbols().isEmpty()) {
            List<TournamentAllowedSymbol> symbols = request.getAllowedSymbols()
                    .stream()
                    .map(symbol -> TournamentAllowedSymbol.builder()
                            .tournament(tournament)
                            .symbol(symbol.toUpperCase())
                            .build())
                    .toList();
            tournament.getAllowedSymbols().addAll(symbols);
        }

        tournamentRepository.save(tournament);
        log.info("Tournament created: id={}, creator={}", tournament.getId(), principal.getId());

        return mapToDTO(tournament);
    }

    // ── Read ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TournamentDTO getById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND, id
                ));
        return mapToDTO(tournament);
    }

    @Transactional(readOnly = true)
    public Page<TournamentDTO> getAll(Pageable pageable) {
        return tournamentRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TournamentDTO> getByStatus(TournamentStatus status, Pageable pageable) {
        return tournamentRepository.findByStatus(status, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TournamentDTO> getMyTournaments(
            UserPrincipal principal,
            Pageable pageable
    ) {
        return tournamentRepository.findByCreatorId(principal.getId(), pageable)
                .map(this::mapToDTO);
    }

    // ── Update ────────────────────────────────────────────────────

    @Transactional
    public TournamentDTO update(
            Long id,
            UpdateTournamentRequest request,
            UserPrincipal principal
    ) {
        Tournament tournament = findOwnedTournament(id, principal);

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new TournamentException(
                    TournamentException.Code.REGISTRATION_CLOSED,
                    "Tournament can only be edited in DRAFT status"
            );
        }

        if (request.getName() != null) {
            tournament.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tournament.setDescription(request.getDescription());
        }
        if (request.getInitialCapital() != null) {
            tournament.setInitialCapital(request.getInitialCapital());
        }
        if (request.getMaxParticipants() != null) {
            tournament.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getStartDate() != null) {
            tournament.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            tournament.setEndDate(request.getEndDate());
        }

        // Validate dates after update
        if (tournament.getEndDate().isBefore(tournament.getStartDate())) {
            throw new TournamentException(TournamentException.Code.INVALID_DATE_RANGE);
        }

        // Replace allowed symbols if provided
        if (request.getAllowedSymbols() != null) {
            tournament.getAllowedSymbols().clear();
            List<TournamentAllowedSymbol> symbols = request.getAllowedSymbols()
                    .stream()
                    .map(symbol -> TournamentAllowedSymbol.builder()
                            .tournament(tournament)
                            .symbol(symbol.toUpperCase())
                            .build())
                    .toList();
            tournament.getAllowedSymbols().addAll(symbols);
        }

        tournamentRepository.save(tournament);
        log.info("Tournament updated: id={}", id);

        return mapToDTO(tournament);
    }

    // ── Publish ───────────────────────────────────────────────────

    @Transactional
    public TournamentDTO publish(Long id, UserPrincipal principal) {
        Tournament tournament = findOwnedTournament(id, principal);

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new TournamentException(
                    TournamentException.Code.REGISTRATION_CLOSED,
                    "Only DRAFT tournaments can be published"
            );
        }

        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        tournamentRepository.save(tournament);

        log.info("Tournament published: id={}", id);
        return mapToDTO(tournament);
    }

    // ── Cancel ────────────────────────────────────────────────────

    @Transactional
    public TournamentDTO cancel(
            Long id,
            CancelTournamentRequest request,
            UserPrincipal principal
    ) {
        Tournament tournament;
        if(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("tournament:cancel"))) {
            tournament = tournamentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND, id
                    ));
        } else {
            tournament = findOwnedTournament(id, principal);
        }

        if (tournament.getStatus() == TournamentStatus.COMPLETED) {
            throw new TournamentException(TournamentException.Code.CANNOT_CANCEL);
        }

        tournament.setStatus(TournamentStatus.CANCELLED);
        tournamentRepository.save(tournament);

        log.info("Tournament cancelled: id={}, reason={}", id, request.getReason());
        return mapToDTO(tournament);
    }

    // ── Delete (soft) ─────────────────────────────────────────────

    @Transactional
    public void delete(Long id, UserPrincipal principal) {
        Tournament tournament = findOwnedTournament(id, principal);

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new TournamentException(
                    TournamentException.Code.CANNOT_CANCEL,
                    "Only DRAFT tournaments can be deleted"
            );
        }

        // @SQLDelete fires UPDATE tournaments SET is_deleted = true
        tournamentRepository.delete(tournament);
        log.info("Tournament soft deleted: id={}", id);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Tournament findOwnedTournament(Long id, UserPrincipal principal) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND, id
                ));

        // Return 404 instead of 403 — prevent resource enumeration
        if (!tournament.getCreator().getId().equals(principal.getId())) {
            throw new ResourceNotFoundException(
                    ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND, id
            );
        }

        return tournament;
    }

    private long getParticipantCount(Long tournamentId) {
        return participantRepository.countByTournamentIdAndStatus(
                tournamentId,
                ParticipantStatus.ACTIVE
        );
    }

    public TournamentDTO mapToDTO(Tournament tournament) {
        List<String> symbols = tournament.getAllowedSymbols()
                .stream()
                .map(TournamentAllowedSymbol::getSymbol)
                .toList();

        return TournamentDTO.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .status(tournament.getStatus())
                .visibility(tournament.getVisibility())
                .initialCapital(tournament.getInitialCapital())
                .maxParticipants(tournament.getMaxParticipants())
                .currentParticipants(getParticipantCount(tournament.getId()))
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .allowedSymbols(symbols)
                .creatorId(tournament.getCreator().getId())
                .creatorName(tournament.getCreator().getDisplayName())
                .createdAt(tournament.getCreatedAt())
                .build();
    }
}