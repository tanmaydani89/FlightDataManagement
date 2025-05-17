package com.aviation.flightdatamanagement.controller;

import com.aviation.flightdatamanagement.dto.FlightRequestDto;
import com.aviation.flightdatamanagement.dto.FlightResponseDto;
import com.aviation.flightdatamanagement.service.FlightDetailsService;
import com.aviation.flightdatamanagement.util.DateTimeUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightDetailsController {

    private final FlightDetailsService flightDetailsService;

    @PostMapping
    @Operation(summary = "Create a new flight entry")
    public ResponseEntity<FlightResponseDto> createFlight(
            @Valid @RequestBody FlightRequestDto flightRequestDto) {
        FlightResponseDto created = flightDetailsService.createFlight(flightRequestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Retrieve all stored flights")
    public ResponseEntity<List<FlightResponseDto>> getAllFlights() {
        List<FlightResponseDto> flights = flightDetailsService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch flight details by ID")
    public ResponseEntity<FlightResponseDto> getFlightById(@PathVariable Long id) {
        FlightResponseDto flight = flightDetailsService.getFlightById(id);
        return ResponseEntity.ok(flight);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update flight information for the specified ID")
    public ResponseEntity<FlightResponseDto> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightRequestDto request) {
        FlightResponseDto updated = flightDetailsService.updateFlight(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a flight entry by ID")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightDetailsService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search flights across internal database and external supplier",
            description = "Search based on origin, destination, airline, and departure/arrival timing. "
                    + "Timestamps follow ISO_DATE_TIME (UTC), and dates use ISO_LOCAL_DATE format."
    )
    public ResponseEntity<List<FlightResponseDto>> searchFlights(
            @Parameter(description = "Origin airport IATA code", schema = @Schema(type = "string"))
            @RequestParam(required = false) String origin,

            @Parameter(description = "Destination airport IATA code", schema = @Schema(type = "string"))
            @RequestParam(required = false) String destination,

            @Parameter(description = "Airline name", schema = @Schema(type = "string"))
            @RequestParam(required = false) String airline,

            @Parameter(description = "Earliest acceptable departure time (e.g., 2023-10-27T10:00:00Z)", schema = @Schema(type = "string", format = "date-time"))
            @RequestParam(required = false) String departureTime,

            @Parameter(description = "Latest acceptable arrival time (e.g., 2023-10-27T18:00:00Z)", schema = @Schema(type = "string", format = "date-time"))
            @RequestParam(required = false) String arrivalTime,

            @Parameter(description = "Date of departure (ISO_LOCAL_DATE, e.g., 2023-10-27)", schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        OffsetDateTime parsedDeparture = DateTimeUtil.parseIsoDateTime(departureTime);
        OffsetDateTime parsedArrival = DateTimeUtil.parseIsoDateTime(arrivalTime);

        List<FlightResponseDto> results = flightDetailsService.searchFlights(
                origin, destination, airline,
                parsedDeparture, parsedArrival, departureDate
        );
        return ResponseEntity.ok(results);
    }
}