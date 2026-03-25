package com.vestara.tradingtournamentplatform.config;

import com.vestara.tradingtournamentplatform.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketTools {

   private final MarketDataService marketDataService;

   @Tool(description = "Retrieve the latest real-time stock price for a given symbol (e.g., GOOGL, AAPL). " +
           "Use this tool only when the current price is required to answer the user's question. Call it at most once per" +
           " query and reuse the result for reasoning."
   )
   public Map<String, Object> getStockPrice(String symbol) {
      BigDecimal price = marketDataService.getPrice(symbol);
      log.info("getStockPrice called");

      if (price == null) {
         return Map.of("symbol", symbol, "error", "Price not available");
      }

      return Map.of(
              "symbol", symbol,
              "price", price
      );
   }
}
