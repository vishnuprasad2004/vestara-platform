package com.app.tradingtournament.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelTournamentRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}