package com.app.tradingtournament.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    // Cache name constants — used in @Cacheable annotations
    public static final String STOCK_PRICES   = "stockPrices";
    public static final String STOCK_SEARCH   = "stockSearch";
    public static final String STOCK_HISTORY  = "stockHistory";
    public static final String LEADERBOARD    = "leaderboard";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Each cache gets its own TTL via createCache()
        manager.registerCustomCache(STOCK_PRICES,
                buildCache(30, TimeUnit.SECONDS, 500));

        manager.registerCustomCache(STOCK_SEARCH,
                buildCache(5, TimeUnit.MINUTES, 200));

        manager.registerCustomCache(STOCK_HISTORY,
                buildCache(1, TimeUnit.HOURS, 100));

        manager.registerCustomCache(LEADERBOARD,
                buildCache(30, TimeUnit.SECONDS, 100));

        return manager;
    }

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildCache(
            long duration,
            TimeUnit unit,
            long maxSize
    ) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration, unit)
                .maximumSize(maxSize)
                .recordStats()  // enables hit/miss metrics
                .build();
    }
}