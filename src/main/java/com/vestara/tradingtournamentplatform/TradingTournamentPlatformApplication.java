package com.vestara.tradingtournamentplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication   // = @Configuration + @EnableAutoConfiguration + @ComponentScan
@EnableCaching           // activates @Cacheable, @CacheEvict, @CachePut
@EnableAsync             // activates @Async (used by EmailService)
@EnableScheduling        // activates @Scheduled (tournament lifecycle, leaderboard refresh)
@EnableRetry
public class TradingTournamentPlatformApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(TradingTournamentPlatformApplication.class, args);
    }

}
