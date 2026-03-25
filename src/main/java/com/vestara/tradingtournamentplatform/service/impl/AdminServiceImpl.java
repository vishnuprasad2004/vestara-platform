package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.dto.response.PlatformAnalyticsDTO;
import com.vestara.tradingtournamentplatform.dto.response.TournamentAnalyticsDTO;
import com.vestara.tradingtournamentplatform.dto.response.UserDTO;
import com.vestara.tradingtournamentplatform.entity.Portfolio;
import com.vestara.tradingtournamentplatform.entity.User;
import com.vestara.tradingtournamentplatform.entity.enums.ParticipantStatus;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.entity.enums.UserRole;
import com.vestara.tradingtournamentplatform.exception.ResourceNotFoundException;
import com.vestara.tradingtournamentplatform.exception.TournamentException;
import com.vestara.tradingtournamentplatform.repository.*;
import com.vestara.tradingtournamentplatform.repository.*;
import com.vestara.tradingtournamentplatform.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final TradeRepository tradeRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final PortfolioRepository portfolioRepository;

    // ── User Management ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public UserDTO banUser(Long userId) {
        User user = findUserOrThrow(userId);

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new TournamentException(
                    TournamentException.Code.CANNOT_CANCEL,
                    "Cannot ban SUPER_ADMIN"
            );
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User banned: id={}", userId);
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO unbanUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setActive(true);
        userRepository.save(user);
        log.info("User unbanned: id={}", userId);
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO promoteToAdmin(Long userId) {
        User user = findUserOrThrow(userId);

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new TournamentException(
                    TournamentException.Code.CANNOT_CANCEL,
                    "Cannot change SUPER_ADMIN role"
            );
        }

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        log.info("User promoted to ADMIN: id={}", userId);
        return mapToDTO(user);
    }

    // ── Analytics ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PlatformAnalyticsDTO getPlatformAnalytics() {
        long totalUsers = userRepository.count();
        long totalTournaments = tournamentRepository.count();
        long activeTournaments = tournamentRepository
                .countByStatus(TournamentStatus.ACTIVE);
        long totalTrades = tradeRepository.count();
        List<Map<String, Object>> mostTraded =
                tradeRepository.findMostTradedSymbols(
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        return PlatformAnalyticsDTO.builder()
                .totalUsers(totalUsers)
                .totalTournaments(totalTournaments)
                .activeTournaments(activeTournaments)
                .totalTrades(totalTrades)
                .mostTradedSymbols(mostTraded)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentAnalyticsDTO getTournamentAnalytics(Long tournamentId) {
        var tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND,
                        tournamentId
                ));

        long totalParticipants = participantRepository
                .countByTournamentIdAndStatus(
                        tournamentId, ParticipantStatus.ACTIVE
                );
        long totalTrades = tradeRepository.findByTournamentId(
                tournamentId,
                org.springframework.data.domain.Pageable.unpaged()
        ).getTotalElements();

        List<Map<String, Object>> mostTraded =
                tradeRepository.findMostTradedSymbols(
                        org.springframework.data.domain.PageRequest.of(0, 5)
                );

        // Top performer from leaderboard
        var topEntry = leaderboardEntryRepository
                .findByTournamentIdOrderByRankPositionAsc(
                        tournamentId,
                        org.springframework.data.domain.PageRequest.of(0, 1)
                )
                .getContent()
                .stream()
                .findFirst();

        String topPerformerName = topEntry
                .map(e -> e.getUser().getDisplayName())
                .orElse(null);
        BigDecimal topPerformerReturn = topEntry
                .map(e -> e.getReturnPercentage())
                .orElse(null);

        // Average portfolio value
        List<Portfolio> portfolios = portfolioRepository
                .findByTournamentId(tournamentId);

        BigDecimal averageValue = BigDecimal.ZERO;
        if (!portfolios.isEmpty()) {
            BigDecimal total = portfolios.stream()
                    .map(Portfolio::getCashBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageValue = total.divide(
                    BigDecimal.valueOf(portfolios.size()),
                    2, RoundingMode.HALF_UP
            );
        }

        return TournamentAnalyticsDTO.builder()
                .tournamentId(tournamentId)
                .tournamentName(tournament.getName())
                .totalParticipants(totalParticipants)
                .totalTrades(totalTrades)
                .mostTradedSymbols(mostTraded)
                .topPerformerName(topPerformerName)
                .topPerformerReturn(topPerformerReturn)
                .averagePortfolioValue(averageValue)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findByIdIgnoreActive(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.USER_NOT_FOUND,
                        userId
                ));
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}