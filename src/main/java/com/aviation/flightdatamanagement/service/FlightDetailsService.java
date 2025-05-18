package com.aviation.flightdatamanagement.service;

import com.aviation.flightdatamanagement.dto.FlightRequestDto;
import com.aviation.flightdatamanagement.dto.FlightResponseDto;
import com.aviation.flightdatamanagement.entity.FlightDetails;
import com.aviation.flightdatamanagement.exception.ResourceNotFoundException;
import com.aviation.flightdatamanagement.repository.FlightDetailsRepository;
import com.aviation.flightdatamanagement.util.DateTimeUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FlightDetailsService {

    private final FlightDetailsRepository flightRepository;
    private final ISupplierService supplierService;
    private static final Logger LOGGER = LogManager.getLogger(FlightDetailsService.class);


    @Autowired
    public FlightDetailsService(@Qualifier("CrazySupplierServiceImpl") ISupplierService supplierService,
                                FlightDetailsRepository flightRepository) {
        this.flightRepository = flightRepository;
        this.supplierService = supplierService;
    }


    @Transactional
    public FlightResponseDto createFlight(FlightRequestDto flightDto) {
        FlightDetails flight = mapToEntity(flightDto);
        flight = flightRepository.save(flight);
        return mapToResponseDto(flight);
    }

    public List<FlightResponseDto> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public FlightResponseDto getFlightById(Long id) {
        FlightDetails flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STR."Flight not found with id: \{id}"));
        return mapToResponseDto(flight);
    }

    @Transactional
    public FlightResponseDto updateFlight(Long id, FlightRequestDto flightDto) {
        FlightDetails existingFlight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STR."Flight not found with id: \{id}"));

        existingFlight.setAirline(flightDto.getAirline());
        existingFlight.setSupplier(flightDto.getSupplier());
        existingFlight.setFare(flightDto.getFare());
        existingFlight.setDepartureAirport(flightDto.getDepartureAirport());
        existingFlight.setDestinationAirport(flightDto.getDestinationAirport());
        existingFlight.setDepartureTime(DateTimeUtil.parseIsoDateTime(flightDto.getDepartureTime()));
        existingFlight.setArrivalTime(DateTimeUtil.parseIsoDateTime(flightDto.getArrivalTime()));

        FlightDetails updatedFlight = flightRepository.save(existingFlight);
        return mapToResponseDto(updatedFlight);
    }

    @Transactional
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException(STR."Flight not found with id: \{id}");
        }
        flightRepository.deleteById(id);
    }

    public List<FlightResponseDto> searchFlights(
            String origin, String destination, String airline,
            OffsetDateTime departureTime, OffsetDateTime arrivalTime,
            LocalDate departureDate
    ) {
        // 1. Search internal DB
        Specification<FlightDetails> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (origin != null && !origin.isBlank()) {
                predicates.add(cb.equal(root.get("departureAirport"), origin));
            }
            if (destination != null && !destination.isBlank()) {
                predicates.add(cb.equal(root.get("destinationAirport"), destination));
            }
            if (airline != null && !airline.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("airline")), STR."%\{airline.toLowerCase()}%"));
            }
            if (departureTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureTime"), departureTime));
            }
            if (arrivalTime != null) {
                // If arrival is on or before the arrivalTime
                predicates.add(cb.lessThanOrEqualTo(root.get("arrivalTime"), arrivalTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<FlightResponseDto> internalFlights = flightRepository.findAll(spec).stream()
                .map(this::mapToResponseDto)
                .toList();

        LOGGER.info("Found {} flights from internal DB.", internalFlights.size());

        // 2. Search CrazySupplier
        List<FlightResponseDto> crazySupplierFlights = new ArrayList<>();
        if (!StringUtils.isBlank(origin) &&
                 !StringUtils.isBlank(destination) && departureDate != null) {
            crazySupplierFlights = supplierService.fetchFlights(origin, destination, departureDate);
            LOGGER.info("Found {} flights from CrazySupplier.", crazySupplierFlights.size());

            // Additional filters
            crazySupplierFlights = crazySupplierFlights.stream()
                    .filter(f -> airline == null || airline.isBlank() || f.getAirline().equalsIgnoreCase(airline))
                    .filter(f -> departureTime == null || !f.getDepartureTime().isBefore(departureTime))
                    .filter(f -> arrivalTime == null || !f.getArrivalTime().isAfter(arrivalTime))
                    .collect(Collectors.toList());
            LOGGER.info("After filtering, {} flights remain from CrazySupplier.", crazySupplierFlights.size());
        }

        // 3. Concat and return
        return Stream.concat(internalFlights.stream(), crazySupplierFlights.stream())
                .collect(Collectors.toList());
    }


    private FlightDetails mapToEntity(FlightRequestDto dto) {
        return FlightDetails.builder()
                .airline(dto.getAirline())
                .supplier(dto.getSupplier())
                .fare(dto.getFare())
                .departureAirport(dto.getDepartureAirport())
                .destinationAirport(dto.getDestinationAirport())
                .departureTime(DateTimeUtil.parseIsoDateTime(dto.getDepartureTime()))
                .arrivalTime(DateTimeUtil.parseIsoDateTime(dto.getArrivalTime()))
                .build();
    }

    private FlightResponseDto mapToResponseDto(FlightDetails flight) {
        return FlightResponseDto.builder()
                .id(flight.getId())
                .airline(flight.getAirline())
                .supplier(flight.getSupplier())
                .fare(flight.getFare())
                .departureAirport(flight.getDepartureAirport())
                .destinationAirport(flight.getDestinationAirport())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .build();
    }
}
