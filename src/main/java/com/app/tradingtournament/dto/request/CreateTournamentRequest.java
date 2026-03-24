package com.app.tradingtournament.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CreateTournamentRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    @DecimalMin(value = "1000.00")
    @DecimalMax(value = "10000000.00")
    private BigDecimal initialCapital;

    @NotNull
    @Min(value = 2)
    private Integer maxParticipants;

    @NotNull
    @Future
    private Instant startDate;

    @NotNull
    @Future
    private Instant endDate;

    // Optional — empty means all symbols allowed
    private List<String> allowedSymbols;
}