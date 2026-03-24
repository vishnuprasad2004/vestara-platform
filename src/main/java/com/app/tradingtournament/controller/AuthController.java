package com.app.tradingtournament.controller;

import com.app.tradingtournament.dto.request.LoginRequest;
import com.app.tradingtournament.dto.request.RegisterRequest;
import com.app.tradingtournament.dto.ApiResponse;
import com.app.tradingtournament.dto.response.AuthResponse;
import com.app.tradingtournament.dto.response.UserDTO;
import com.app.tradingtournament.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh, logout")
public class AuthController {

    private final AuthServiceImpl authService;

    // ── Register ──────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserDTO>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserDTO user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user));
    }

    // ── Login ─────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(auth));
    }

    // ── Refresh ───────────────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.refresh(request, response);
        return ResponseEntity.ok(ApiResponse.success(auth));
    }

    // ── Logout ────────────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Email Verification ────────────────────────────────────────

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address via token link")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestParam String email
    ) {
        authService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}