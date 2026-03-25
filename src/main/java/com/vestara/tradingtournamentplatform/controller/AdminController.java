package com.vestara.tradingtournamentplatform.controller;

import com.vestara.tradingtournamentplatform.dto.ApiResponse;
import com.vestara.tradingtournamentplatform.dto.response.*;
import com.vestara.tradingtournamentplatform.dto.response.PlatformAnalyticsDTO;
import com.vestara.tradingtournamentplatform.dto.response.TournamentAnalyticsDTO;
import com.vestara.tradingtournamentplatform.dto.response.UserDTO;
import com.vestara.tradingtournamentplatform.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "User management and platform analytics")
public class AdminController {

    private final AdminService adminService;

    // ── User Management ───────────────────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:read:any')")
    @Operation(summary = "Get all users — paginated")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getAllUsers(pageable)
        ));
    }

    @PostMapping("/users/{userId}/ban")
    @PreAuthorize("hasAuthority('user:update:any')")
    @Operation(summary = "Ban a user")
    public ResponseEntity<ApiResponse<UserDTO>> banUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.banUser(userId)
        ));
    }

    @PostMapping("/users/{userId}/unban")
    @PreAuthorize("hasAuthority('user:update:any')")
    @Operation(summary = "Unban a user")
    public ResponseEntity<ApiResponse<UserDTO>> unbanUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.unbanUser(userId)
        ));
    }

    @PostMapping("/users/{userId}/promote")
    @PreAuthorize("hasAuthority('user:promote:admin')")
    @Operation(summary = "Promote user to ADMIN — SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<UserDTO>> promoteToAdmin(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.promoteToAdmin(userId)
        ));
    }

    // ── Analytics ─────────────────────────────────────────────────

    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('analytics:read')")
    @Operation(summary = "Get platform-wide analytics")
    public ResponseEntity<ApiResponse<PlatformAnalyticsDTO>> getPlatformAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getPlatformAnalytics()
        ));
    }

    @GetMapping("/tournaments/{tournamentId}/analytics")
    @PreAuthorize("hasAuthority('analytics:read')")
    @Operation(summary = "Get analytics for a specific tournament")
    public ResponseEntity<ApiResponse<TournamentAnalyticsDTO>> getTournamentAnalytics(
            @PathVariable Long tournamentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getTournamentAnalytics(tournamentId)
        ));
    }
}