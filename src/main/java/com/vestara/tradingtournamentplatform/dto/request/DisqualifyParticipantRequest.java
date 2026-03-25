package com.vestara.tradingtournamentplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisqualifyParticipantRequest {

    @NotBlank(message = "Disqualification reason is required")
    private String reason;
}