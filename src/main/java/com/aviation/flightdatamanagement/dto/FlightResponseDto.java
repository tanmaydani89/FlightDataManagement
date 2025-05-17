package com.aviation.flightdatamanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class FlightResponseDto {
    private Long id;
    private String airline;
    private String supplier;
    private BigDecimal fare;
    private String departureAirport;
    private String destinationAirport;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime departureTime; // UTC

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime arrivalTime; // UTC
}
