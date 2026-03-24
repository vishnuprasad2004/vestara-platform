package com.app.tradingtournament.service.impl;

import com.app.tradingtournament.dto.request.LoginRequest;
import com.app.tradingtournament.dto.request.RegisterRequest;
import com.app.tradingtournament.dto.response.AuthResponse;
import com.app.tradingtournament.dto.response.UserDTO;
import com.app.tradingtournament.entity.EmailVerificationToken;
import com.app.tradingtournament.entity.RefreshToken;
import com.app.tradingtournament.entity.User;
import com.app.tradingtournament.entity.enums.AuthProvider;
import com.app.tradingtournament.entity.enums.UserRole;
import com.app.tradingtournament.exception.AuthException;
import com.app.tradingtournament.exception.RegistrationException;
import com.app.tradingtournament.exception.TokenException;
import com.app.tradingtournament.repository.EmailVerificationTokenRepository;
import com.app.tradingtournament.repository.RefreshTokenRepository;
import com.app.tradingtournament.repository.UserRepository;
import com.app.tradingtournament.security.JwtService;
import com.app.tradingtournament.security.UserPrincipal;
import com.app.tradingtournament.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailServiceImpl emailService;

    @Value("${app.jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.env.type}")
    private String envType;

    // ── Register ──────────────────────────────────────────────────

    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Only VIEWER and TRADER can self-register
        // ADMIN is assigned by SUPER_ADMIN after registration
        if (request.getRole() == UserRole.ADMIN
                || request.getRole() == UserRole.SUPER_ADMIN) {
            throw new RegistrationException(
                    RegistrationException.Code.EMAIL_ALREADY_EXISTS
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException(
                    RegistrationException.Code.EMAIL_ALREADY_EXISTS
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .role(request.getRole())
                .authProvider(AuthProvider.EMAIL)
                .emailVerified(false)
                .isActive(true)
                .build();

        userRepository.save(user);

        // Send verification email async
        issueAndSendVerificationToken(user);

        log.info("User registered: id={}, role={}", user.getId(), user.getRole());

        return mapToUserDTO(user);
    }

    // ── Login ─────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(
            LoginRequest request,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AuthException(AuthException.Code.INVALID_CREDENTIALS)
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(AuthException.Code.INVALID_CREDENTIALS);
        }
        if(!Objects.equals(envType, "DEV")) {
            if (!user.isEmailVerified()) {
                throw new AuthException(AuthException.Code.EMAIL_NOT_VERIFIED);
            }
        }


        if (!user.isActive()) {
            throw new AuthException(AuthException.Code.ACCOUNT_DISABLED);
        }

        return issueTokens(user, response);
    }

    // ── Refresh ───────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String rawToken = extractRefreshTokenFromCookie(request);
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new TokenException(TokenException.Code.INVALID_TOKEN)
                );

        // Reuse detection — token already used = stolen token
        if (refreshToken.isUsed()) {
            // Revoke all tokens for this user — full session invalidation
            refreshTokenRepository.revokeAllByUser(refreshToken.getUser());
            log.warn("Refresh token reuse detected for userId: {}",
                    refreshToken.getUser().getId());
            throw new TokenException(TokenException.Code.REFRESH_TOKEN_ALREADY_USED);
        }

        if (refreshToken.isRevoked()) {
            throw new TokenException(TokenException.Code.REFRESH_TOKEN_REVOKED);
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenException(TokenException.Code.REFRESH_TOKEN_EXPIRED);
        }

        // Mark old token as used — rotation
        refreshToken.setUsed(true);
        refreshTokenRepository.save(refreshToken);

        return issueTokens(refreshToken.getUser(), response);
    }

    // ── Logout ────────────────────────────────────────────────────

    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String rawToken = extractRefreshTokenFromCookie(request);
            String tokenHash = hashToken(rawToken);

            refreshTokenRepository.findByTokenHash(tokenHash)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        } catch (Exception e) {
            // Logout should never fail — even if token is missing just clear cookie
            log.debug("Token not found during logout — clearing cookie anyway");
        }

        clearRefreshTokenCookie(response);
        log.info("User logged out");
    }

    // ── Email Verification ────────────────────────────────────────

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new TokenException(TokenException.Code.INVALID_TOKEN)
                        );

        if (verificationToken.isUsed()) {
            throw new TokenException(
                    TokenException.Code.VERIFICATION_TOKEN_ALREADY_USED
            );
        }

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenException(
                    TokenException.Code.VERIFICATION_TOKEN_EXPIRED
            );
        }

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for userId: {}", user.getId());
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                                new TokenException(TokenException.Code.INVALID_TOKEN)
                        // Intentionally vague — prevent email enumeration
                );

        if (user.isEmailVerified()) {
            throw new TokenException(
                    TokenException.Code.VERIFICATION_TOKEN_ALREADY_USED
            );
        }

        // Delete any existing tokens for this user before issuing a new one
        emailVerificationTokenRepository.deleteAllByUser(user);
        issueAndSendVerificationToken(user);
    }

    // ── Private helpers ───────────────────────────────────────────

    private AuthResponse issueTokens(User user, HttpServletResponse response) {
        UserPrincipal principal = UserPrincipal.fromUser(user);

        // Access token — short lived, returned in body
        String accessToken = jwtService.generateAccessToken(principal);

        // Refresh token — long lived, stored in HttpOnly cookie
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashedRefreshToken)
                .user(user)
                .expiresAt(Instant.now().plus(refreshTokenExpiryDays, ChronoUnit.DAYS))
                .revoked(false)
                .used(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        setRefreshTokenCookie(response, rawRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs)
                .build();
    }

    private void issueAndSendVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .used(false)
                .build();

        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user, token);
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response,
            String rawToken
    ) {
        Cookie cookie = new Cookie("refreshToken", rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);                         // HTTPS only
        cookie.setPath("/api/v1/auth/refresh");         // narrow path
        cookie.setMaxAge(refreshTokenExpiryDays * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(0);                            // immediate expiry
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new TokenException(TokenException.Code.INVALID_TOKEN);
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new TokenException(TokenException.Code.INVALID_TOKEN)
                );
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}