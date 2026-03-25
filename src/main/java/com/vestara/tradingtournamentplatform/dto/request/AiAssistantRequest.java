package com.vestara.tradingtournamentplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAssistantRequest {
    @NotNull
    private Long tournamentId;

    @NotBlank
    @Size(max = 500)
    private String message;
}