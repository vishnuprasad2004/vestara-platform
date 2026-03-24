package com.app.tradingtournament.repository;

import com.app.tradingtournament.entity.Tournament;
import com.app.tradingtournament.entity.enums.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    // Scheduler: find tournaments ready to go ACTIVE
    List<Tournament> findByStatusAndStartDateLessThanEqual(
        TournamentStatus status,
        Instant now
    );

    // Scheduler: find tournaments ready to COMPLETE
    List<Tournament> findByStatusAndEndDateLessThanEqual(
        TournamentStatus status,
        Instant now
    );

    // Admin analytics
    long countByStatus(TournamentStatus status);

    // Check if a tournament name is already taken
    boolean existsByName(String name);

    // All tournaments a specific user created
    Page<Tournament> findByCreatorId(Long userId, Pageable pageable);
}