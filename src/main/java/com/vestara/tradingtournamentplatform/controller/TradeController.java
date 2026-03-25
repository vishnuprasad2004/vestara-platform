package com.vestara.tradingtournamentplatform.controller;

import com.vestara.tradingtournamentplatform.dto.request.TradeRequest;
import com.vestara.tradingtournamentplatform.dto.ApiResponse;
import com.vestara.tradingtournamentplatform.dto.response.TradeDTO;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
@Tag(name = "Trades", description = "Trade execution and history")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    @PreAuthorize("hasAuthority('trade:execute')")
    @Operation(summary = "Execute a buy order")
    public ResponseEntity<ApiResponse<TradeDTO>> buy(
            @Valid @RequestBody TradeRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TradeDTO trade = tradeService.buy(request, idempotencyKey, principal);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(trade));
    }

    @PostMapping("/sell")
    @PreAuthorize("hasAuthority('trade:execute')")
    @Operation(summary = "Execute a sell order")
    public ResponseEntity<ApiResponse<TradeDTO>> sell(
            @Valid @RequestBody TradeRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TradeDTO trade = tradeService.sell(request, idempotencyKey, principal);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(trade));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('trade:read')")
    @Operation(summary = "Get my trade history")
    public ResponseEntity<ApiResponse<Page<TradeDTO>>> getMyTrades(
            @RequestParam Long tournamentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "executedAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tradeService.getMyTrades(tournamentId, principal, pageable)
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('trade:read:all')")
    @Operation(summary = "Get all trades in a tournament — Admin only")
    public ResponseEntity<ApiResponse<Page<TradeDTO>>> getAllTrades(
            @RequestParam Long tournamentId,
            @PageableDefault(size = 20, sort = "executedAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tradeService.getAllTrades(tournamentId, pageable)
        ));
    }
}