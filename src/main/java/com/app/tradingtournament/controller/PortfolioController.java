package com.app.tradingtournament.controller;

import com.app.tradingtournament.dto.ApiResponse;
import com.app.tradingtournament.dto.response.PortfolioDTO;
import com.app.tradingtournament.security.UserPrincipal;
import com.app.tradingtournament.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournaments/{tournamentId}/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio and holdings management")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @PreAuthorize("hasAuthority('portfolio:read:own')")
    @Operation(summary = "Get my portfolio in a tournament")
    public ResponseEntity<ApiResponse<PortfolioDTO>> getMyPortfolio(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                portfolioService.getMyPortfolio(tournamentId, principal)
        ));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('portfolio:read:all')")
    @Operation(summary = "Get any participant portfolio — Admin/Owner only")
    public ResponseEntity<ApiResponse<PortfolioDTO>> getPortfolioByUserId(
            @PathVariable Long tournamentId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                portfolioService.getPortfolioByUserId(tournamentId, userId)
        ));
    }
}