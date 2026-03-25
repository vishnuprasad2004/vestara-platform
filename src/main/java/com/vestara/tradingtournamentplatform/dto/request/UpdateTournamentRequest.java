package com.vestara.tradingtournamentplatform.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class UpdateTournamentRequest {

    @Size(max = 100)
    private String name;

    private String description;

    @DecimalMin(value = "1000.00")
    @DecimalMax(value = "10000000.00")
    private BigDecimal initialCapital;

    @Min(value = 2)
    private Integer maxParticipants;

    @Future
    private Instant startDate;

    @Future
    private Instant endDate;

    private List<String> allowedSymbols;
}