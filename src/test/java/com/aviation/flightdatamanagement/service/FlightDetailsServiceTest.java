package com.aviation.flightdatamanagement.service;

import com.aviation.flightdatamanagement.dto.FlightRequestDto;
import com.aviation.flightdatamanagement.dto.FlightResponseDto;
import com.aviation.flightdatamanagement.entity.FlightDetails;
import com.aviation.flightdatamanagement.exception.ResourceNotFoundException;
import com.aviation.flightdatamanagement.repository.FlightDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlightDetailsServiceTest {
    @Mock
    private FlightDetailsRepository flightRepository;

    @Mock
    private SupplierService crazySupplierService;

    @InjectMocks
    private FlightDetailsService flightService;

    private FlightDetails flight1;
    private FlightRequestDto flightRequestDto1;
    private FlightResponseDto flightResponseDto1;
    private OffsetDateTime testDepartureTime;
    private OffsetDateTime testArrivalTime;

    @BeforeEach
    void setUp() {
        testDepartureTime = OffsetDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        testArrivalTime = OffsetDateTime.of(2024, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC);

        flight1 = FlightDetails.builder()
                .id(1L)
                .airline("TestAir")
                .supplier("InternalDB")
                .fare(new BigDecimal("200.00"))
                .departureAirport("AAA")
                .destinationAirport("BBB")
                .departureTime(testDepartureTime)
                .arrivalTime(testArrivalTime)
                .build();

        flightRequestDto1 = new FlightRequestDto();
        flightRequestDto1.setAirline("TestAir");
        flightRequestDto1.setSupplier("InternalDB");
        flightRequestDto1.setFare(new BigDecimal("200.00"));
        flightRequestDto1.setDepartureAirport("AAA");
        flightRequestDto1.setDestinationAirport("BBB");
        flightRequestDto1.setDepartureTime(testDepartureTime.toString());
        flightRequestDto1.setArrivalTime(testArrivalTime.toString());

        flightResponseDto1 = FlightResponseDto.builder()
                .id(1L)
                .airline("TestAir")
                .supplier("InternalDB")
                .fare(new BigDecimal("200.00"))
                .departureAirport("AAA")
                .destinationAirport("BBB")
                .departureTime(testDepartureTime)
                .arrivalTime(testArrivalTime)
                .build();
    }

    @Test
    void createFlight_shouldSaveAndReturnFlight() {
        when(flightRepository.save(any(FlightDetails.class))).thenReturn(flight1);

        FlightResponseDto result = flightService.createFlight(flightRequestDto1);

        assertNotNull(result);
        assertEquals("TestAir", result.getAirline());
        verify(flightRepository, times(1)).save(any(FlightDetails.class));
    }

    @Test
    void getFlightById_whenFound_shouldReturnFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight1));
        FlightResponseDto result = flightService.getFlightById(1L);
        assertEquals(flightResponseDto1, result);
    }

    @Test
    void getFlightById_whenNotFound_shouldThrowException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> flightService.getFlightById(1L));
    }

    @Test
    void updateFlight_whenFound_shouldUpdateAndReturnFlight() {
        FlightRequestDto updatedDto = new FlightRequestDto();
        updatedDto.setAirline("UpdatedAir");
        updatedDto.setSupplier("InternalDB");
        updatedDto.setFare(new BigDecimal("250.00"));
        updatedDto.setDepartureAirport("AAA");
        updatedDto.setDestinationAirport("BBB");
        updatedDto.setDepartureTime(testDepartureTime.plusHours(1).toString());
        updatedDto.setArrivalTime(testArrivalTime.plusHours(1).toString());


        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight1));
        when(flightRepository.save(any(FlightDetails.class))).thenAnswer(invocation -> invocation.getArgument(0)); // return the saved entity

        FlightResponseDto result = flightService.updateFlight(1L, updatedDto);

        assertNotNull(result);
        assertEquals("UpdatedAir", result.getAirline());
        assertEquals(new BigDecimal("250.00"), result.getFare());
        assertEquals(testDepartureTime.plusHours(1), result.getDepartureTime());
        verify(flightRepository, times(1)).save(any(FlightDetails.class));
    }

    @Test
    void deleteFlight_whenFound_shouldDelete() {
        when(flightRepository.existsById(1L)).thenReturn(true);
        doNothing().when(flightRepository).deleteById(1L);

        flightService.deleteFlight(1L);

        verify(flightRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteFlight_whenNotFound_shouldThrowException() {
        when(flightRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> flightService.deleteFlight(1L));
    }


    @Test
    @SuppressWarnings("unchecked")
    void searchFlights_shouldCombineInternalAndCrazySupplierResults() {
        String origin = "AAA";
        String destination = "BBB";
        String airline = "TestAir";
        OffsetDateTime searchDepTime = testDepartureTime.minusHours(1);
        OffsetDateTime searchArrTime = testArrivalTime.plusHours(1);
        LocalDate searchDepDate = searchDepTime.toLocalDate();


        FlightResponseDto crazyFlight = FlightResponseDto.builder()
                .airline("CrazyAir")
                .supplier(com.aviation.flightdatamanagement.service.SupplierService.SUPPLIER_NAME)
                .fare(new BigDecimal("300.00"))
                .departureAirport(origin)
                .destinationAirport(destination)
                .departureTime(testDepartureTime.plusDays(1))
                .arrivalTime(testArrivalTime.plusDays(1))
                .build();

        when(flightRepository.findAll(any(Specification.class))).thenReturn(List.of(flight1));
        when(crazySupplierService.fetchFlights(origin, destination, searchDepDate)).thenReturn(List.of(crazyFlight));

        List<FlightResponseDto> results = flightService.searchFlights(origin, destination, null, searchDepTime, searchArrTime, searchDepDate);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(f -> "InternalDB".equals(f.getSupplier())));
        assertTrue(results.stream().anyMatch(f -> com.aviation.flightdatamanagement.service.SupplierService.SUPPLIER_NAME.equals(f.getSupplier())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchFlights_crazySupplierFiltering() {
        String origin = "AAA";
        String destination = "BBB";
        LocalDate searchDepDate = LocalDate.of(2024,3,15);

        FlightResponseDto crazyFlight1 = FlightResponseDto.builder() // Matches airline
                .airline("MatchAir") .supplier(com.aviation.flightdatamanagement.service.SupplierService.SUPPLIER_NAME) .fare(BigDecimal.ONE)
                .departureAirport(origin).destinationAirport(destination)
                .departureTime(OffsetDateTime.parse("2024-03-15T10:00:00Z"))
                .arrivalTime(OffsetDateTime.parse("2024-03-15T12:00:00Z")).build();
        FlightResponseDto crazyFlight2 = FlightResponseDto.builder() // Does not match airline
                .airline("OtherAir") .supplier(com.aviation.flightdatamanagement.service.SupplierService.SUPPLIER_NAME) .fare(BigDecimal.ONE)
                .departureAirport(origin).destinationAirport(destination)
                .departureTime(OffsetDateTime.parse("2024-03-15T11:00:00Z"))
                .arrivalTime(OffsetDateTime.parse("2024-03-15T13:00:00Z")).build();
        FlightResponseDto crazyFlight3 = FlightResponseDto.builder() // Matches airline, but dep time too early
                .airline("MatchAir") .supplier(com.aviation.flightdatamanagement.service.SupplierService.SUPPLIER_NAME) .fare(BigDecimal.ONE)
                .departureAirport(origin).destinationAirport(destination)
                .departureTime(OffsetDateTime.parse("2024-03-15T08:00:00Z")) // Earlier than searchDepTime
                .arrivalTime(OffsetDateTime.parse("2024-03-15T10:00:00Z")).build();


        when(flightRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(crazySupplierService.fetchFlights(origin, destination, searchDepDate)).thenReturn(List.of(crazyFlight1, crazyFlight2, crazyFlight3));

        List<FlightResponseDto> results = flightService.searchFlights(
                origin, destination, "MatchAir", // Airline filter
                OffsetDateTime.parse("2024-03-15T09:00:00Z"), // Departure time filter (UTC)
                null, // No arrival time filter
                searchDepDate);

        assertEquals(1, results.size());
        assertEquals("MatchAir", results.get(0).getAirline());
        assertEquals(crazyFlight1.getDepartureTime(), results.get(0).getDepartureTime());
    }
}
