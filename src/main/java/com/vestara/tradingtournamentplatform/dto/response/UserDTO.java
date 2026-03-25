package com.vestara.tradingtournamentplatform.dto.response;

import com.vestara.tradingtournamentplatform.entity.enums.AuthProvider;
import com.vestara.tradingtournamentplatform.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserDTO {
    private final Long id;
    private final String email;
    private final String displayName;
    private final UserRole role;
    private final AuthProvider authProvider;
    private final Boolean emailVerified;
    private final Boolean active;
    private final Instant createdAt;
}