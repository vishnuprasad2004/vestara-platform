package com.vestara.tradingtournamentplatform.dto.request;

import com.vestara.tradingtournamentplatform.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class
RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    @Size(min = 2, max = 50)
    private String displayName;

    @NotNull
    private UserRole role;   // only VIEWER or TRADER allowed — validated in service
}