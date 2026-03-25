package com.vestara.tradingtournamentplatform.scheduler;

import com.vestara.tradingtournamentplatform.repository.PortfolioRepository;
import com.vestara.tradingtournamentplatform.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceRefreshScheduler {

    private final MarketDataService marketDataService;
    private final PortfolioRepository portfolioRepository;

    // Runs every 30 seconds
    @Scheduled(fixedDelay = 30_000)
    public void prefetchActivePrices() {
        // Fetch all symbols currently held across all active tournament portfolios
        List<String> symbols = portfolioRepository.findActiveSymbolsByTournamentId();

        if (symbols.isEmpty()) return;

        log.debug("Pre-warming price cache for {} symbols", symbols.size());

        marketDataService.prefetchPrices(symbols);
    }
}