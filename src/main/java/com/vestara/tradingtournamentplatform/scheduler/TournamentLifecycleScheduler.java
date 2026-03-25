package com.vestara.tradingtournamentplatform.scheduler;

import com.vestara.tradingtournamentplatform.entity.Tournament;
import com.vestara.tradingtournamentplatform.entity.TournamentParticipant;
import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.repository.LeaderboardEntryRepository;
import com.vestara.tradingtournamentplatform.repository.TournamentParticipantRepository;
import com.vestara.tradingtournamentplatform.repository.TournamentRepository;
import com.vestara.tradingtournamentplatform.service.EmailService;
import com.vestara.tradingtournamentplatform.service.LeaderboardService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TournamentLifecycleScheduler {

    private final TournamentRepository tournamentRepository;
    private final LeaderboardService leaderboardService;
    private final EmailService emailService;
    private final TournamentParticipantRepository participantRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;

    @PostConstruct
    public void init() {
        log.info("TournamentLifecycleScheduler initialized");
    }

    // Runs every 60 seconds
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void processTournamentLifecycle() {
        activateTournaments();
        completeTournaments();
    }

    // REGISTRATION_OPEN → ACTIVE when startDate has passed
    private void activateTournaments() {
        log.info("Scheduler fired — checking for tournaments to activate. Now: {}", Instant.now());
        List<Tournament> toActivate = tournamentRepository
                .findByStatusAndStartDateLessThanEqual(
                        TournamentStatus.REGISTRATION_OPEN,
                        Instant.now()
                );

        log.info("Tournaments to activate: {}", toActivate.size());
        for (Tournament tournament : toActivate) {
            tournament.setStatus(TournamentStatus.ACTIVE);
            tournamentRepository.save(tournament);
            List<TournamentParticipant> participants = participantRepository
                    .findByTournamentIdAndStatus(
                            tournament.getId(), ParticipantStatus.ACTIVE
                    );
            for (TournamentParticipant p : participants) {
                emailService.sendTournamentStartedEmail(p.getUser(), tournament);
            }
            log.info("Tournament ACTIVATED: id={}, name={}",
                    tournament.getId(), tournament.getName());
        }
    }

    // ACTIVE → COMPLETED when endDate has passed
    private void completeTournaments() {
        List<Tournament> toComplete = tournamentRepository
                .findByStatusAndEndDateLessThanEqual(
                        TournamentStatus.ACTIVE,
                        Instant.now()
                );

        for (Tournament tournament : toComplete) {
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournamentRepository.save(tournament);

            // Final leaderboard calculation
            leaderboardService.recalculate(tournament.getId());

            List<TournamentParticipant> participants = participantRepository
                    .findByTournamentIdAndStatus(
                            tournament.getId(), ParticipantStatus.ACTIVE
                    );
            for (TournamentParticipant p : participants) {
                int rank = leaderboardEntryRepository
                        .findByUserIdAndTournamentId(
                                p.getUser().getId(), tournament.getId())
                        .map(e -> e.getRankPosition())
                        .orElse(0);
                emailService.sendTournamentEndedEmail(p.getUser(), tournament, rank);
            }

            log.info("Tournament COMPLETED: id={}, name={}",
                    tournament.getId(), tournament.getName());
        }
    }
}