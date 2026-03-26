package com.vestara.tradingtournamentplatform.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MarketDataService {

    // Get current price for a single symbol
    BigDecimal getPrice(String symbol);

    // Search stocks by name or symbol
    List<Map<String, String>> search(String query);

    // Pre-warm cache for a list of symbols (called by scheduler)
    void prefetchPrices(List<String> symbols);

    // Get Company News for a symbol - consumed by the AI Agent
    List<Map<String, String>> getCompanyNews(String symbol);
}