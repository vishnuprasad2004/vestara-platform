package com.app.tradingtournament.security;

import com.app.tradingtournament.entity.User;
import com.app.tradingtournament.entity.enums.AuthProvider;
import com.app.tradingtournament.entity.enums.UserRole;
import com.app.tradingtournament.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauthUser = oauthToken.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        // Extract GitHub profile fields
        String githubId = String.valueOf(attributes.get("id"));
        String email = extractEmail(attributes);
        String displayName = extractDisplayName(attributes);

        if (email == null) {
            log.warn("GitHub OAuth2 login failed — no email returned for githubId: {}", githubId);
            response.sendRedirect(frontendUrl + "/login?error=no_email");
            return;
        }

        // Find or create user
        User user = findOrCreateUser(githubId, email, displayName);

        // Build UserPrincipal and issue our JWT
        UserPrincipal principal = UserPrincipal.fromUserWithOAuth2(user, attributes);
        String accessToken = jwtService.generateAccessToken(principal);

        log.info("OAuth2 login successful for userId: {}, provider: GITHUB", user.getId());

        // Redirect frontend with token in query param
        // Frontend reads this once, stores in memory, discards from URL
        String redirectUrl = frontendUrl + "/oauth2/success?token=" + accessToken;
        response.sendRedirect(redirectUrl);
    }

    // ── Private helpers ───────────────────────────────────────────

    private User findOrCreateUser(
            String githubId,
            String email,
            String displayName
    ) {
        // Case 1: existing GitHub user
        return userRepository.findByGithubId(githubId)
                .orElseGet(() ->
                    // Case 2: existing email user — link GitHub to their account
                    userRepository.findByEmail(email)
                            .map(existingUser -> {
                                existingUser.setGithubId(githubId);
                                existingUser.setAuthProvider(AuthProvider.GITHUB);
                                return userRepository.save(existingUser);
                            })
                            // Case 3: brand new user — create account
                            .orElseGet(() -> {
                                User newUser = User.builder()
                                        .email(email)
                                        .displayName(displayName)
                                        .githubId(githubId)
                                        .authProvider(AuthProvider.GITHUB)
                                        .role(UserRole.TRADER)
                                        .emailVerified(true)  // GitHub verified their email
                                        .isActive(true)
                                        .build();
                                return userRepository.save(newUser);
                            })
                );
    }

    private String extractEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        return email != null ? String.valueOf(email) : null;
    }

    private String extractDisplayName(Map<String, Object> attributes) {
        // Prefer "name", fall back to "login" (GitHub username)
        Object name = attributes.get("name");
        if (name != null && !String.valueOf(name).isBlank()) {
            return String.valueOf(name);
        }
        Object login = attributes.get("login");
        return login != null ? String.valueOf(login) : "GitHub User";
    }
}