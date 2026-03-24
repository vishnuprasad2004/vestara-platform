package com.app.tradingtournament.controller;

import com.app.tradingtournament.dto.ApiResponse;
import com.app.tradingtournament.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Stock prices and search")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/stocks/{symbol}/price")
    @PreAuthorize("hasAuthority('market:read')")
    @Operation(summary = "Get current stock price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPrice(
            @PathVariable String symbol
    ) {
        BigDecimal price = marketDataService.getPrice(symbol);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "symbol", symbol.toUpperCase(),
                "price", price
        )));
    }

    @GetMapping("/stocks/search")
    @PreAuthorize("hasAuthority('market:read')")
    @Operation(summary = "Search stocks by symbol or name")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> search(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                marketDataService.search(query)
        ));
    }
}