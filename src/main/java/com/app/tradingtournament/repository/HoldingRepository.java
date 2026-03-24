package com.app.tradingtournament.repository;

import com.app.tradingtournament.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);

    // All active holdings in a portfolio — quantity > 0
    List<Holding> findByPortfolioIdAndQuantityGreaterThan(Long portfolioId, int quantity);

    boolean existsByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}