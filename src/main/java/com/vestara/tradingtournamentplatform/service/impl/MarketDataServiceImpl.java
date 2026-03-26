package com.vestara.tradingtournamentplatform.service.impl;

import com.vestara.tradingtournamentplatform.config.CacheConfig;
import com.vestara.tradingtournamentplatform.exception.ExternalServiceException;
import com.vestara.tradingtournamentplatform.service.MarketDataService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MarketDataServiceImpl implements MarketDataService {

    private final RestClient restClient;
    private final String apiKey;

    public MarketDataServiceImpl(
            @Value("${app.finnhub.base-url}") String baseUrl,
            @Value("${app.finnhub.api-key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // ── Get price ─────────────────────────────────────────────────

    @Override
    @Cacheable(value = CacheConfig.STOCK_PRICES, key = "#symbol.toUpperCase()")
    @CircuitBreaker(name = "finnhub", fallbackMethod = "getPriceFallback")
    @Retry(name = "finnhub")
    public BigDecimal getPrice(String symbol) {
        log.debug("Fetching live price for symbol: {}", symbol);

        try {
            Map response = restClient.get()
                    .uri("/quote?symbol={symbol}&token={token}",
                            symbol.toUpperCase(), apiKey)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("c")) {
                throw new ExternalServiceException(
                        ExternalServiceException.Code.MARKET_DATA_INVALID_SYMBOL
                );
            }

            // "c" = current price in Finnhub response
            Object currentPrice = response.get("c");
            BigDecimal price = new BigDecimal(currentPrice.toString());

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ExternalServiceException(
                        ExternalServiceException.Code.MARKET_DATA_INVALID_SYMBOL
                );
            }

            log.debug("Live price for {}: {}", symbol.toUpperCase(), price);
            return price;

        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Finnhub error for symbol {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException(
                    ExternalServiceException.Code.MARKET_DATA_UNAVAILABLE
            );
        }
    }

    public BigDecimal getPriceFallback(String symbol, Exception ex) {
        log.warn("Circuit open or retries exhausted for symbol: {}. " +
                "Reason: {}", symbol, ex.getMessage());
        throw new ExternalServiceException(
                ExternalServiceException.Code.MARKET_DATA_UNAVAILABLE
        );
    }

    // ── Search ────────────────────────────────────────────────────

    @Override
    @Cacheable(value = CacheConfig.STOCK_SEARCH, key = "#query.toLowerCase()")
    @CircuitBreaker(name = "finnhub", fallbackMethod = "searchFallback")
    @Retry(name = "finnhub")
    public List<Map<String, String>> search(String query) {
        log.debug("Searching stocks for query: {}", query);

        try {
            Map response = restClient.get()
                    .uri("/search?q={query}&token={token}", query, apiKey)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("result")) {
                return List.of();
            }

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) response.get("result");

            List<Map<String, String>> mapped = new ArrayList<>();
            for (Map<String, Object> item : results) {
                mapped.add(Map.of(
                        "symbol", str(item.get("symbol")),
                        "name",   str(item.get("description")),
                        "type",   str(item.get("type")),
                        "exchange", str(item.get("primaryExchange"))
                ));
            }

            return mapped;

        } catch (Exception e) {
            log.error("Finnhub search error for query {}: {}", query, e.getMessage());
            throw new ExternalServiceException(
                    ExternalServiceException.Code.MARKET_DATA_UNAVAILABLE
            );
        }
    }

    public List<Map<String, String>> searchFallback(String query, Exception ex) {
        log.warn("Search fallback triggered for query: {}", query);
        return List.of();
    }

    // ── Prefetch ──────────────────────────────────────────────────

    @Override
    public void prefetchPrices(List<String> symbols) {
        if (symbols.isEmpty()) return;

        log.debug("Prefetching prices for {} symbols", symbols.size());

        for (String symbol : symbols) {
            try {
                getPrice(symbol);
            } catch (Exception e) {
                // Non-fatal — cache will populate on demand
                log.warn("Prefetch failed for {}: {}", symbol, e.getMessage());
            }
        }
    }

    @Override
    public List<Map<String, String>> getCompanyNews(String symbol) {
        log.debug("Fetching live company news for symbol: {}", symbol);

        Map response = restClient.get()
                .uri("/company-news?symbol={symbol}&from={from}&to={to}&token={token}",
                        symbol.toUpperCase(), getPreviousDate(), getCurrentDate(),  apiKey)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("result")) {
            return List.of();
        }

        List<Map<String, Object>> results =
                (List<Map<String, Object>>) response.get("result");

        List<Map<String, String>> mapped = new ArrayList<>();

        int limit = Math.min(10, results.size());

        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = results.get(i);

            mapped.add(Map.of(
                    "source", str(item.get("symbol")),
                    "summary", str(item.get("description")),
                    "url", str(item.get("type"))
            ));
        }

        return mapped;
    }

    // ── Util ──────────────────────────────────────────────────────

    public String getCurrentDate() {
        return LocalDate.now(ZoneId.of("America/New_York"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getPreviousDate() {
        return LocalDate.now(ZoneId.of("America/New_York")).minusDays(2)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}