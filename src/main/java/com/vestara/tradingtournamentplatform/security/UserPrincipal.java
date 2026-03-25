package com.vestara.tradingtournamentplatform.security;

import com.vestara.tradingtournamentplatform.entity.User;
import com.vestara.tradingtournamentplatform.entity.enums.AuthProvider;
import com.vestara.tradingtournamentplatform.entity.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String password;
    private final String displayName;
    private final UserRole role;
    private final AuthProvider authProvider;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    // OAuth2 attributes — only populated for GitHub login
    private Map<String, Object> oauth2Attributes;

    // ── Constructors ──────────────────────────────────────────────

    // Used by JwtAuthenticationFilter after validating JWT
    public UserPrincipal(
            Long id,
            String email,
            String password,
            String displayName,
            UserRole role,
            AuthProvider authProvider,
            boolean emailVerified,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.role = role;
        this.authProvider = authProvider;
        this.emailVerified = emailVerified;
        this.authorities = authorities;
    }

    // ── Static factories ──────────────────────────────────────────

    // Build from User entity — used at login time
    public static UserPrincipal fromUser(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getRole(),
                user.getAuthProvider(),
                user.isEmailVerified(),
                buildAuthorities(user.getRole())
        );
    }

    // Build from User entity for OAuth2 — used by OAuth2SuccessHandler
    public static UserPrincipal fromUserWithOAuth2(
            User user,
            Map<String, Object> attributes
    ) {
        UserPrincipal principal = fromUser(user);
        principal.oauth2Attributes = attributes;
        return principal;
    }

    // Build from JWT claims — used by JwtAuthenticationFilter
    // No DB call needed — everything comes from the token
    public static UserPrincipal fromJwtClaims(
            Long id,
            String email,
            String displayName,
            UserRole role,
            AuthProvider authProvider,
            boolean emailVerified,
            List<String> authorityStrings
    ) {
        List<GrantedAuthority> authorities = authorityStrings.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UserPrincipal(
                id,
                email,
                null,        // password not stored in JWT
                displayName,
                role,
                authProvider,
                emailVerified,
                authorities
        );
    }

    // ── Authority builder ─────────────────────────────────────────

    public static List<GrantedAuthority> buildAuthorities(UserRole role) {
        return switch (role) {
            case VIEWER -> List.of(
                    new SimpleGrantedAuthority("tournament:read"),
                    new SimpleGrantedAuthority("user:read:own"),
                    new SimpleGrantedAuthority("leaderboard:read"),
                    new SimpleGrantedAuthority("market:read"),
                    new SimpleGrantedAuthority("user:update:own")
            );
            case TRADER -> List.of(
                    new SimpleGrantedAuthority("tournament:read"),
                    new SimpleGrantedAuthority("tournament:create"),
                    new SimpleGrantedAuthority("tournament:update:own"),
                    new SimpleGrantedAuthority("tournament:delete:own"),
                    new SimpleGrantedAuthority("tournament:join"),
                    new SimpleGrantedAuthority("trade:execute"),
                    new SimpleGrantedAuthority("trade:read"),
                    new SimpleGrantedAuthority("user:read:own"),
                    new SimpleGrantedAuthority("portfolio:read:own"),
                    new SimpleGrantedAuthority("market:read"),
                    new SimpleGrantedAuthority("leaderboard:read"),
                    new SimpleGrantedAuthority("user:update:own")
            );
            case ADMIN -> List.of(
                    new SimpleGrantedAuthority("tournament:read"),
                    new SimpleGrantedAuthority("tournament:create"),
                    new SimpleGrantedAuthority("tournament:update:own"),
                    new SimpleGrantedAuthority("tournament:update:any"),
                    new SimpleGrantedAuthority("tournament:delete:own"),
                    new SimpleGrantedAuthority("tournament:delete:any"),
                    new SimpleGrantedAuthority("tournament:cancel"),
                    new SimpleGrantedAuthority("tournament:join"),
                    new SimpleGrantedAuthority("trade:execute"),
                    new SimpleGrantedAuthority("trade:read"),
                    new SimpleGrantedAuthority("user:read:own"),
                    new SimpleGrantedAuthority("user:read:any"),
                    new SimpleGrantedAuthority("user:update:own"),
                    new SimpleGrantedAuthority("user:update:any"),
                    new SimpleGrantedAuthority("participant:disqualify"),
                    new SimpleGrantedAuthority("portfolio:read:own"),
                    new SimpleGrantedAuthority("portfolio:read:all"),
                    new SimpleGrantedAuthority("leaderboard:read"),
                    new SimpleGrantedAuthority("market:read"),
                    new SimpleGrantedAuthority("analytics:read")
            );
            case SUPER_ADMIN -> List.of(
                    new SimpleGrantedAuthority("tournament:read"),
                    new SimpleGrantedAuthority("tournament:create"),
                    new SimpleGrantedAuthority("tournament:update:own"),
                    new SimpleGrantedAuthority("tournament:update:any"),
                    new SimpleGrantedAuthority("tournament:delete:own"),
                    new SimpleGrantedAuthority("tournament:delete:any"),
                    new SimpleGrantedAuthority("tournament:cancel"),
                    new SimpleGrantedAuthority("tournament:join"),
                    new SimpleGrantedAuthority("trade:execute"),
                    new SimpleGrantedAuthority("trade:read"),
                    new SimpleGrantedAuthority("user:read:own"),
                    new SimpleGrantedAuthority("user:read:any"),
                    new SimpleGrantedAuthority("user:update:own"),
                    new SimpleGrantedAuthority("user:update:any"),
                    new SimpleGrantedAuthority("user:promote:admin"),
                    new SimpleGrantedAuthority("user:delete:any"),
                    new SimpleGrantedAuthority("participant:disqualify"),
                    new SimpleGrantedAuthority("analytics:read"),
                    new SimpleGrantedAuthority("portfolio:read:own"),
                    new SimpleGrantedAuthority("portfolio:read:all"),
                    new SimpleGrantedAuthority("leaderboard:read"),
                    new SimpleGrantedAuthority("market:read"),
                    new SimpleGrantedAuthority("platform:manage")
            );
        };
    }

    // ── UserDetails interface ─────────────────────────────────────

    @Override
    public String getUsername() {
        return email;   // Spring Security uses username — we use email
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ── OAuth2User interface ──────────────────────────────────────

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2Attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}