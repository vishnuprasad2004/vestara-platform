package com.vestara.tradingtournamentplatform.service;

import com.vestara.tradingtournamentplatform.dto.response.AuthResponse;
import com.vestara.tradingtournamentplatform.dto.request.LoginRequest;
import com.vestara.tradingtournamentplatform.dto.request.RegisterRequest;
import com.vestara.tradingtournamentplatform.dto.response.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * Registers a new user, hashes password, and triggers email verification.
     */
    UserDTO register(RegisterRequest request);

    /**
     * Validates credentials and issues access/refresh tokens.
     * Skips email verification check if env is 'DEV'.
     */
    AuthResponse login(LoginRequest request, HttpServletResponse response);

    /**
     * Rotates refresh tokens and issues a new access token.
     * Implements reuse detection to prevent token theft.
     */
    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response);

    /**
     * Invalidates the current refresh token and clears the security cookie.
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * Confirms the user's email address using a UUID token.
     */
    void verifyEmail(String token);

    /**
     * Generates a new verification link for the user.
     */
    void resendVerification(String email);
}