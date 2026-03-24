package com.app.tradingtournament.service.impl;

import com.app.tradingtournament.dto.response.HoldingDTO;
import com.app.tradingtournament.dto.response.PortfolioDTO;
import com.app.tradingtournament.entity.Holding;
import com.app.tradingtournament.entity.Portfolio;
import com.app.tradingtournament.exception.ResourceNotFoundException;
import com.app.tradingtournament.repository.PortfolioRepository;
import com.app.tradingtournament.security.UserPrincipal;
import com.app.tradingtournament.service.MarketDataService;
import com.app.tradingtournament.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;

    // ── Get my portfolio ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PortfolioDTO getMyPortfolio(
            Long tournamentId,
            UserPrincipal principal
    ) {
        Portfolio portfolio = findPortfolioOrThrow(
                principal.getId(), tournamentId
        );
        return mapToDTO(portfolio);
    }

    // ── Get any portfolio — admin/owner only ──────────────────────

    @Override
    @Transactional(readOnly = true)
    public PortfolioDTO getPortfolioByUserId(Long tournamentId, Long userId) {
        Portfolio portfolio = findPortfolioOrThrow(userId, tournamentId);
        return mapToDTO(portfolio);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Portfolio findPortfolioOrThrow(Long userId, Long tournamentId) {
        return portfolioRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResourceNotFoundException.Code.PORTFOLIO_NOT_FOUND
                ));
    }

    private PortfolioDTO mapToDTO(Portfolio portfolio) {
        // Map holdings with live prices
        List<HoldingDTO> holdingDTOs = portfolio.getHoldings()
                .stream()
                .filter(h -> h.getQuantity() > 0)   // exclude closed positions
                .map(this::mapHoldingToDTO)
                .toList();

        // Total unrealized P&L across all holdings
        BigDecimal totalUnrealizedPnl = holdingDTOs.stream()
                .map(HoldingDTO::getUnrealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total market value of all holdings
        BigDecimal totalMarketValue = holdingDTOs.stream()
                .map(HoldingDTO::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total portfolio value = cash + market value of all holdings
        BigDecimal totalPortfolioValue = portfolio.getCashBalance()
                .add(totalMarketValue);

        // Return % = ((totalValue - initialCapital) / initialCapital) * 100
        BigDecimal returnPercentage = BigDecimal.ZERO;
        if (portfolio.getInitialCapital().compareTo(BigDecimal.ZERO) > 0) {
            returnPercentage = totalPortfolioValue
                    .subtract(portfolio.getInitialCapital())
                    .divide(portfolio.getInitialCapital(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return PortfolioDTO.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUser().getId())
                .displayName(portfolio.getUser().getDisplayName())
                .tournamentId(portfolio.getTournament().getId())
                .tournamentName(portfolio.getTournament().getName())
                .cashBalance(portfolio.getCashBalance())
                .initialCapital(portfolio.getInitialCapital())
                .totalPortfolioValue(totalPortfolioValue)
                .totalUnrealizedPnl(totalUnrealizedPnl)
                .returnPercentage(returnPercentage)
                .holdings(holdingDTOs)
                .build();
    }

    private HoldingDTO mapHoldingToDTO(Holding holding) {
        BigDecimal currentPrice = marketDataService.getPrice(holding.getSymbol());
        BigDecimal marketValue = currentPrice.multiply(
                BigDecimal.valueOf(holding.getQuantity())
        );
        BigDecimal unrealizedPnl = currentPrice
                .subtract(holding.getAverageBuyPrice())
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        // Unrealized P&L % = ((currentPrice - avgBuyPrice) / avgBuyPrice) * 100
        BigDecimal unrealizedPnlPercent = BigDecimal.ZERO;
        if (holding.getAverageBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
            unrealizedPnlPercent = currentPrice
                    .subtract(holding.getAverageBuyPrice())
                    .divide(holding.getAverageBuyPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return HoldingDTO.builder()
                .id(holding.getId())
                .symbol(holding.getSymbol())
                .quantity(holding.getQuantity())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(currentPrice)
                .marketValue(marketValue)
                .unrealizedPnl(unrealizedPnl)
                .unrealizedPnlPercent(unrealizedPnlPercent)
                .build();
    }
}