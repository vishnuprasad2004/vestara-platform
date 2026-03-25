package com.vestara.tradingtournamentplatform.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;    // milliseconds
}