    package com.app.tradingtournament.service.impl;

import com.app.tradingtournament.dto.request.TradeRequest;
import com.app.tradingtournament.dto.response.TradeDTO;
import com.app.tradingtournament.entity.*;
import com.app.tradingtournament.entity.enums.*;
import com.app.tradingtournament.exception.*;
import com.app.tradingtournament.repository.*;
import com.app.tradingtournament.security.UserPrincipal;
import com.app.tradingtournament.service.MarketDataService;
import com.app.tradingtournament.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;
    private final ApplicationEventPublisher eventPublisher;

    // ── Buy ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public TradeDTO buy(
            TradeRequest request,
            String idempotencyKey,
            UserPrincipal principal
    ) {
        // Idempotency check — duplicate request returns early
        if (idempotencyKey != null &&
                tradeRepository.existsByIdempotencyKey(idempotencyKey)) {
            return tradeRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::mapToDTO)
                    .orElseThrow();
        }

        Tournament tournament = findActiveTournamentOrThrow(
                request.getTournamentId()
        );
        TournamentParticipant participant = findActiveParticipantOrThrow(
                principal.getId(), request.getTournamentId()
        );
        validateSymbol(request.getSymbol(), tournament);

        BigDecimal price = marketDataService.getPrice(request.getSymbol());
        BigDecimal totalCost = price.multiply(
                BigDecimal.valueOf(request.getQuantity())
        );

        Portfolio portfolio = portfolioRepository
                .findByUserIdAndTournamentId(
                        principal.getId(), request.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.PORTFOLIO_NOT_FOUND
                ));

        // Balance check
        if (portfolio.getCashBalance().compareTo(totalCost) < 0) {
            throw new TradeException(
                    TradeException.Code.INSUFFICIENT_BALANCE,
                    totalCost,
                    portfolio.getCashBalance()
            );
        }

        // Deduct cash
        portfolio.setCashBalance(
                portfolio.getCashBalance().subtract(totalCost)
        );

        // Update or create holding
        Optional<Holding> existingHolding = holdingRepository
                .findByPortfolioIdAndSymbol(portfolio.getId(), request.getSymbol());

        if (existingHolding.isPresent()) {
            Holding holding = existingHolding.get();
            // Recalculate weighted average buy price
            BigDecimal totalShares = BigDecimal.valueOf(
                    holding.getQuantity() + request.getQuantity()
            );
            BigDecimal totalValue = price.multiply(
                    BigDecimal.valueOf(request.getQuantity()))
                    .add(holding.getAverageBuyPrice().multiply(
                            BigDecimal.valueOf(holding.getQuantity()))
                    );
            holding.setAverageBuyPrice(
                    totalValue.divide(totalShares, 4, RoundingMode.HALF_UP)
            );
            holding.setQuantity(holding.getQuantity() + request.getQuantity());
            holdingRepository.save(holding);
        } else {
            Holding newHolding = Holding.builder()
                    .portfolio(portfolio)
                    .symbol(request.getSymbol().toUpperCase())
                    .quantity(request.getQuantity())
                    .averageBuyPrice(price)
                    .build();
            holdingRepository.save(newHolding);
        }

        portfolioRepository.save(portfolio);

        // Record trade
        Trade trade = Trade.builder()
                .user(portfolio.getUser())
                .tournament(tournament)
                .symbol(request.getSymbol().toUpperCase())
                .tradeType(TradeType.BUY)
                .status(TradeStatus.EXECUTED)
                .quantity(request.getQuantity())
                .pricePerShare(price)
                .totalValue(totalCost)
                .idempotencyKey(idempotencyKey)
                .executedAt(Instant.now())
                .build();

        tradeRepository.save(trade);

        log.info("BUY executed: userId={}, symbol={}, qty={}, price={}",
                principal.getId(), request.getSymbol(),
                request.getQuantity(), price);

        return mapToDTO(trade);
    }

    // ── Sell ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public TradeDTO sell(
            TradeRequest request,
            String idempotencyKey,
            UserPrincipal principal
    ) {
        // Idempotency check
        if (idempotencyKey != null &&
                tradeRepository.existsByIdempotencyKey(idempotencyKey)) {
            return tradeRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::mapToDTO)
                    .orElseThrow();
        }

        Tournament tournament = findActiveTournamentOrThrow(
                request.getTournamentId()
        );
        findActiveParticipantOrThrow(
                principal.getId(), request.getTournamentId()
        );
        validateSymbol(request.getSymbol(), tournament);

        BigDecimal price = marketDataService.getPrice(request.getSymbol());
        BigDecimal totalValue = price.multiply(
                BigDecimal.valueOf(request.getQuantity())
        );

        Portfolio portfolio = portfolioRepository
                .findByUserIdAndTournamentId(
                        principal.getId(), request.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.PORTFOLIO_NOT_FOUND
                ));

        Holding holding = holdingRepository
                .findByPortfolioIdAndSymbol(
                        portfolio.getId(), request.getSymbol())
                .orElseThrow(() -> new TradeException(
                        TradeException.Code.INSUFFICIENT_HOLDINGS
                ));

        // Holdings check
        if (holding.getQuantity() < request.getQuantity()) {
            throw new TradeException(TradeException.Code.INSUFFICIENT_HOLDINGS);
        }

        // Update holding
        holding.setQuantity(holding.getQuantity() - request.getQuantity());
        holdingRepository.save(holding);

        // Credit cash
        portfolio.setCashBalance(
                portfolio.getCashBalance().add(totalValue)
        );
        portfolioRepository.save(portfolio);

        // Record trade
        Trade trade = Trade.builder()
                .user(portfolio.getUser())
                .tournament(tournament)
                .symbol(request.getSymbol().toUpperCase())
                .tradeType(TradeType.SELL)
                .status(TradeStatus.EXECUTED)
                .quantity(request.getQuantity())
                .pricePerShare(price)
                .totalValue(totalValue)
                .idempotencyKey(idempotencyKey)
                .executedAt(Instant.now())
                .build();

        tradeRepository.save(trade);

        log.info("SELL executed: userId={}, symbol={}, qty={}, price={}",
                principal.getId(), request.getSymbol(),
                request.getQuantity(), price);

        return mapToDTO(trade);
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<TradeDTO> getMyTrades(
            Long tournamentId,
            UserPrincipal principal,
            Pageable pageable
    ) {
        return tradeRepository
                .findByUserIdAndTournamentId(
                        principal.getId(), tournamentId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeDTO> getAllTrades(Long tournamentId, Pageable pageable) {
        return tradeRepository
                .findByTournamentId(tournamentId, pageable)
                .map(this::mapToDTO);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Tournament findActiveTournamentOrThrow(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.TOURNAMENT_NOT_FOUND,
                        tournamentId
                ));

        if (tournament.getStatus() != TournamentStatus.ACTIVE) {
            throw new TournamentException(
                    TournamentException.Code.TOURNAMENT_NOT_ACTIVE
            );
        }

        return tournament;
    }

    private TournamentParticipant findActiveParticipantOrThrow(
            Long userId,
            Long tournamentId
    ) {
        TournamentParticipant participant = participantRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new TournamentException(
                        TournamentException.Code.PARTICIPANT_NOT_ACTIVE
                ));

        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new TournamentException(
                    TournamentException.Code.PARTICIPANT_NOT_ACTIVE
            );
        }

        return participant;
    }

    private void validateSymbol(String symbol, Tournament tournament) {
        // Empty allowed symbols list = all symbols tradeable
        if (tournament.getAllowedSymbols().isEmpty()) return;

        boolean allowed = tournament.getAllowedSymbols()
                .stream()
                .anyMatch(s -> s.getSymbol().equalsIgnoreCase(symbol));

        if (!allowed) {
            throw new TournamentException(
                    TournamentException.Code.SYMBOL_NOT_ALLOWED,
                    symbol
            );
        }
    }

    private TradeDTO mapToDTO(Trade trade) {
        return TradeDTO.builder()
                .id(trade.getId())
                .userId(trade.getUser().getId())
                .tournamentId(trade.getTournament().getId())
                .symbol(trade.getSymbol())
                .tradeType(trade.getTradeType())
                .status(trade.getStatus())
                .quantity(trade.getQuantity())
                .pricePerShare(trade.getPricePerShare())
                .totalValue(trade.getTotalValue())
                .idempotencyKey(trade.getIdempotencyKey())
                .executedAt(trade.getExecutedAt())
                .build();
    }
}