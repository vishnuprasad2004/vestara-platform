package com.app.tradingtournament.security;

import com.app.tradingtournament.entity.enums.AuthProvider;
import com.app.tradingtournament.entity.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    // ── Token Generation ──────────────────────────────────────────

    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        List<String> authorities = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(principal.getId()))
                .issuedAt(now)
                .expiration(expiry)
                .claim("userId", principal.getId())
                .claim("email", principal.getEmail())
                .claim("displayName", principal.getDisplayName())
                .claim("role", principal.getRole().name())
                .claim("authorities", authorities)
                .claim("authProvider", principal.getAuthProvider().name())
                .claim("emailVerified", principal.isEmailVerified())
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token Validation ──────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims empty: {}", e.getMessage());
        }
        return false;
    }

    // ── Claims Extraction ─────────────────────────────────────────

    public UserPrincipal getPrincipalFromToken(String token) {
        Claims claims = parseClaims(token);

        Long userId = claims.get("userId", Long.class);
        String email = claims.get("email", String.class);
        String displayName = claims.get("displayName", String.class);
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        AuthProvider authProvider = AuthProvider.valueOf(
                claims.get("authProvider", String.class)
        );
        boolean emailVerified = claims.get("emailVerified", Boolean.class);

        @SuppressWarnings("unchecked")
        List<String> authorities = claims.get("authorities", List.class);

        return UserPrincipal.fromJwtClaims(
                userId,
                email,
                displayName,
                role,
                authProvider,
                emailVerified,
                authorities
        );
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Date getExpiry(String token) {
        return parseClaims(token).getExpiration();
    }

    // ── Private helpers ───────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8); // Expects secret to be valid Base64
        return Keys.hmacShaKeyFor(keyBytes);
    }
}