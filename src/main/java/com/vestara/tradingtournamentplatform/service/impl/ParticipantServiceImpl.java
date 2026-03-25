package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.dto.request.DisqualifyParticipantRequest;
import com.vestara.tradingtournamentplatform.dto.response.ParticipantDTO;
import com.vestara.tradingtournamentplatform.entity.Portfolio;
import com.vestara.tradingtournamentplatform.entity.Tournament;
import com.vestara.tradingtournamentplatform.entity.TournamentParticipant;
import com.vestara.tradingtournamentplatform.entity.User;
import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.exception.ResourceNotFoundException;
import com.vestara.tradingtournamentplatform.exception.TournamentException;
import com.vestara.tradingtournamentplatform.repository.PortfolioRepository;
import com.vestara.tradingtournamentplatform.repository.TournamentParticipantRepository;
import com.vestara.tradingtournamentplatform.repository.TournamentRepository;
import com.vestara.tradingtournamentplatform.repository.UserRepository;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.EmailService;
import com.vestara.tradingtournamentplatform.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ── Join ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public ParticipantDTO join(Long tournamentId, UserPrincipal principal) {
        Tournament tournament = findTournamentOrThrow(tournamentId);

        // Tournament must be open for registration
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentException(TournamentException.Code.REGISTRATION_CLOSED);
        }

        // Already joined check
        if (participantRepository.existsByUserIdAndTournamentId(
                principal.getId(), tournamentId)) {
            throw new TournamentException(TournamentException.Code.ALREADY_JOINED);
        }

        // Max participants check
        long activeCount = participantRepository.countByTournamentIdAndStatus(
                tournamentId, ParticipantStatus.ACTIVE
        );
        if (tournament.getMaxParticipants() > 0
                && activeCount >= tournament.getMaxParticipants()) {
            throw new TournamentException(TournamentException.Code.TOURNAMENT_FULL);
        }

        User user = findUserOrThrow(principal.getId());

        // Create participant record
        TournamentParticipant participant = TournamentParticipant.builder()
                .user(user)
                .tournament(tournament)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        participantRepository.save(participant);

        // Create portfolio with initial capital
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .tournament(tournament)
                .cashBalance(tournament.getInitialCapital())
                .initialCapital(tournament.getInitialCapital())
                .build();

        portfolioRepository.save(portfolio);

        emailService.sendTournamentJoinedEmail(user, tournament);

        log.info("User {} joined tournament {}", principal.getId(), tournamentId);

        return mapToDTO(participant);
    }

    // ── Withdraw ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void withdraw(Long tournamentId, UserPrincipal principal) {
        Tournament tournament = findTournamentOrThrow(tournamentId);

        // Cannot withdraw from an active tournament
        if (tournament.getStatus() == TournamentStatus.ACTIVE) {
            throw new TournamentException(
                    TournamentException.Code.TOURNAMENT_NOT_ACTIVE,
                    "Cannot withdraw from an active tournament"
            );
        }

        TournamentParticipant participant = findParticipantOrThrow(
                principal.getId(), tournamentId
        );

        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new TournamentException(
                    TournamentException.Code.PARTICIPANT_NOT_ACTIVE
            );
        }

        participant.setStatus(ParticipantStatus.WITHDRAWN);
        participant.setStatusReason("Withdrawn by participant");
        participantRepository.save(participant);

        log.info("User {} withdrew from tournament {}", principal.getId(), tournamentId);
    }

    // ── Disqualify ────────────────────────────────────────────────

    @Override
    @Transactional
    public ParticipantDTO disqualify(
            Long tournamentId,
            Long userId,
            DisqualifyParticipantRequest request,
            UserPrincipal principal
    ) {
        Tournament found = findTournamentOrThrow(tournamentId);

        TournamentParticipant participant = findParticipantOrThrow(
                userId, tournamentId
        );

        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new TournamentException(
                    TournamentException.Code.PARTICIPANT_NOT_ACTIVE
            );
        }

        participant.setStatus(ParticipantStatus.DISQUALIFIED);
        participant.setStatusReason(request.getReason());
        participantRepository.save(participant);

        emailService.sendDisqualifiedEmail(
                participant.getUser(), found, request.getReason()
        );

        log.info("User {} disqualified from tournament {} by {}",
                userId, tournamentId, principal.getId());

        return mapToDTO(participant);
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ParticipantDTO> getParticipants(
            Long tournamentId,
            ParticipantStatus status,
            Pageable pageable
    ) {
        findTournamentOrThrow(tournamentId);

        if (status != null) {
            return participantRepository
                    .findByTournamentIdAndStatus(tournamentId, status, pageable)
                    .map(this::mapToDTO);
        }

        return participantRepository
                .findByTournamentId(tournamentId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipantDTO getMyParticipation(
            Long tournamentId,
            UserPrincipal principal
    ) {
        TournamentParticipant participant = findParticipantOrThrow(
                principal.getId(), tournamentId
        );
        return mapToDTO(participant);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Tournament findTournamentOrThrow(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND,
                        tournamentId
                ));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.USER_NOT_FOUND,
                        userId
                ));
    }

    private TournamentParticipant findParticipantOrThrow(
            Long userId,
            Long tournamentId
    ) {
        return participantRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.USER_NOT_FOUND,
                        userId
                ));
    }

    private ParticipantDTO mapToDTO(TournamentParticipant participant) {
        return ParticipantDTO.builder()
                .id(participant.getId())
                .userId(participant.getUser().getId())
                .displayName(participant.getUser().getDisplayName())
                .email(participant.getUser().getEmail())
                .tournamentId(participant.getTournament().getId())
                .tournamentName(participant.getTournament().getName())
                .status(participant.getStatus())
                .statusReason(participant.getStatusReason())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}