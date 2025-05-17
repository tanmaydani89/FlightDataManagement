package com.aviation.flightdatamanagement.entity;

import jakarta.persistence.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Airline is mandatory")
    private String airline;

    @NotBlank(message = "Supplier is mandatory")
    private String supplier; // e.g., "InternalDB", "CrazySupplier"

    @NotNull(message = "Fare is mandatory")
    @Positive(message = "Fare must be positive")
    private BigDecimal fare;

    @NotBlank(message = "Departure airport code is mandatory")
    @Size(min = 3, max = 3, message = "Departure airport code must be 3 letters")
    private String departureAirport;

    @NotBlank(message = "Destination airport code is mandatory")
    @Size(min = 3, max = 3, message = "Destination airport code must be 3 letters")
    private String destinationAirport;

    @NotNull(message = "Departure time is mandatory")
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime departureTime; // Store in UTC

    @NotNull(message = "Arrival time is mandatory")
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime arrivalTime; // Store in UTC
}
