package com.aviation.flightdatamanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlightRequestDto {

    private Long id;


    @NotBlank(message = "Airline is mandatory")
    private String airline;

    @NotBlank(message = "Supplier is mandatory")
    private String supplier;

    @NotNull(message = "Fare is mandatory")
    @Positive(message = "Fare must be positive")
    private BigDecimal fare;

    @NotBlank(message = "Departure airport code is mandatory")
    @Size(min = 3, max = 3, message = "Departure airport code must be 3 letters")
    private String departureAirport;

    @NotBlank(message = "Destination airport code is mandatory")
    @Size(min = 3, max = 3, message = "Destination airport code must be 3 letters")
    private String destinationAirport;

    @NotBlank(message = "Departure time is mandatory (ISO_DATE_TIME format, UTC)")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:\\d{2})",
            message = "Departure time must be in ISO_DATE_TIME format (e.g., 2023-10-27T10:15:30Z)")
    private String departureTime;

    @NotBlank(message = "Arrival time is mandatory (ISO_DATE_TIME format, UTC)")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:\\d{2})",
            message = "Arrival time must be in ISO_DATE_TIME format (e.g., 2023-10-27T10:15:30Z)")
    private String arrivalTime;
}
