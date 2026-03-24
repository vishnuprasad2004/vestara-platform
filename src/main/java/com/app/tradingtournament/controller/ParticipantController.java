package com.app.tradingtournament.controller;

import com.app.tradingtournament.dto.request.DisqualifyParticipantRequest;
import com.app.tradingtournament.dto.ApiResponse;
import com.app.tradingtournament.dto.response.ParticipantDTO;
import com.app.tradingtournament.entity.enums.ParticipantStatus;
import com.app.tradingtournament.security.UserPrincipal;
import com.app.tradingtournament.service.ParticipantService;
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
@RequestMapping("/tournaments/{tournamentId}/participants")
@RequiredArgsConstructor
@Tag(name = "Participants", description = "Tournament participant management")
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping("/join")
    @PreAuthorize("hasAuthority('tournament:join')")
    @Operation(summary = "Join a tournament")
    public ResponseEntity<ApiResponse<ParticipantDTO>> join(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ParticipantDTO participant = participantService.join(tournamentId, principal);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(participant));
    }

    @DeleteMapping("/withdraw")
    @PreAuthorize("hasAuthority('tournament:join')")
    @Operation(summary = "Withdraw from a tournament")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        participantService.withdraw(tournamentId, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{userId}/disqualify")
    @PreAuthorize("hasAuthority('participant:disqualify')")
    @Operation(summary = "Disqualify a participant")
    public ResponseEntity<ApiResponse<ParticipantDTO>> disqualify(
            @PathVariable Long tournamentId,
            @PathVariable Long userId,
            @Valid @RequestBody DisqualifyParticipantRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                participantService.disqualify(
                        tournamentId, userId, request, principal)
        ));
    }

    @GetMapping
    @Operation(summary = "Get all participants in a tournament")
    public ResponseEntity<ApiResponse<Page<ParticipantDTO>>> getParticipants(
            @PathVariable Long tournamentId,
            @RequestParam(required = false) ParticipantStatus status,
            @PageableDefault(size = 20, sort = "joinedAt",
                    direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                participantService.getParticipants(tournamentId, status, pageable)
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('tournament:join')")
    @Operation(summary = "Get my participation in a tournament")
    public ResponseEntity<ApiResponse<ParticipantDTO>> getMyParticipation(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                participantService.getMyParticipation(tournamentId, principal)
        ));
    }
}