package com.vestara.tradingtournamentplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Step 1: Extract the Authorization header ───────────────
        final String authHeader = request.getHeader("Authorization");

        /*
         * If there's no Authorization header, or it doesn't start
         * with "Bearer ", this request is either:
         *   - A public endpoint (/auth/signup, /auth/login)
         *   - A missing token (will be rejected by SecurityConfig)
         * Either way — pass it through, do nothing here.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = extractToken(request);


        if (StringUtils.hasText(token) && jwtService.validateToken(token)) {
            try {
                UserPrincipal principal = jwtService.getPrincipalFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,               // credentials — null after authentication
                                principal.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Token was valid at validateToken() but failed during parsing
                // Clear context to be safe and let the request proceed unauthenticated
                log.warn("Could not set user authentication from token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        // Always continue the chain — even unauthenticated requests proceed
        // Spring Security's authorization rules decide what happens next
        filterChain.doFilter(request, response);
    }

    // ── Private helpers ───────────────────────────────────────────

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // List all your permitAll endpoints here
        return path.startsWith("/auth/") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/swagger-ui/");
    }
}