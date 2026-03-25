package com.vestara.tradingtournamentplatform.controller;

import com.vestara.tradingtournamentplatform.dto.request.CancelTournamentRequest;
import com.vestara.tradingtournamentplatform.dto.request.CreateTournamentRequest;
import com.vestara.tradingtournamentplatform.dto.request.UpdateTournamentRequest;
import com.vestara.tradingtournamentplatform.dto.ApiResponse;
import com.vestara.tradingtournamentplatform.dto.response.TournamentDTO;
import com.vestara.tradingtournamentplatform.entity.enums.TournamentStatus;
import com.vestara.tradingtournamentplatform.security.UserPrincipal;
import com.vestara.tradingtournamentplatform.service.impl.TournamentServiceImpl;
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
@RequestMapping("/tournaments")
@RequiredArgsConstructor
@Tag(name = "Tournaments", description = "Tournament management")
public class TournamentController {

    private final TournamentServiceImpl tournamentService;

    // ── Create ────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('tournament:create')")
    @Operation(summary = "Create a new tournament")
    public ResponseEntity<ApiResponse<TournamentDTO>> create(
            @Valid @RequestBody CreateTournamentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TournamentDTO tournament = tournamentService.create(request, principal);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(tournament));
    }

    // ── Read ──────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all tournaments")
    public ResponseEntity<ApiResponse<Page<TournamentDTO>>> getAll(
            @RequestParam(required = false) TournamentStatus status,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TournamentDTO> tournaments = status != null
                ? tournamentService.getByStatus(status, pageable)
                : tournamentService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(tournaments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tournament by id")
    public ResponseEntity<ApiResponse<TournamentDTO>> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tournamentService.getById(id)
        ));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('tournament:create')")
    @Operation(summary = "Get tournaments created by current user")
    public ResponseEntity<ApiResponse<Page<TournamentDTO>>> getMyTournaments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tournamentService.getMyTournaments(principal, pageable)
        ));
    }

    // ── Update ────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tournament:update:own')")
    @Operation(summary = "Update tournament — DRAFT status only")
    public ResponseEntity<ApiResponse<TournamentDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTournamentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tournamentService.update(id, request, principal)
        ));
    }

    // ── Publish ───────────────────────────────────────────────────

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('tournament:update:own')")
    @Operation(summary = "Publish tournament — DRAFT → REGISTRATION_OPEN")
    public ResponseEntity<ApiResponse<TournamentDTO>> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tournamentService.publish(id, principal)
        ));
    }

    // ── Cancel ────────────────────────────────────────────────────

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('tournament:cancel') or hasAuthority('tournament:cancel:own')")
    @Operation(summary = "Cancel a tournament")
    public ResponseEntity<ApiResponse<TournamentDTO>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelTournamentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tournamentService.cancel(id, request, principal)
        ));
    }

    // ── Delete ────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tournament:delete:own')")
    @Operation(summary = "Soft delete a tournament — DRAFT status only")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        tournamentService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}