package com.vestara.tradingtournamentplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider") // activates createdAt, updatedAt auto-population on entities
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            // Unauthenticated requests — register, login etc.
            // createdBy will be null — acceptable for auth operations
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getName().equals("anonymousUser")) {
                return Optional.empty();
            }

            // getName() on our UserPrincipal returns the userId as String
            // No DB call — reads directly from SecurityContext
            return Optional.of(authentication.getName());
        };
    }
}