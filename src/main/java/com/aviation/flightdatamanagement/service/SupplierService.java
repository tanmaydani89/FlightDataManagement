package com.aviation.flightdatamanagement.service;

import com.aviation.flightdatamanagement.dto.CrazySupplierFlightDto;
import com.aviation.flightdatamanagement.dto.FlightResponseDto;
import com.aviation.flightdatamanagement.dto.SupplierRequest;
import com.aviation.flightdatamanagement.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SupplierService {


    private final RestTemplate restTemplate;
    private final String crazySupplierApiUrl;

    public static final String SUPPLIER_NAME = "CrazySupplier";

    @Autowired
    public SupplierService(RestTemplate restTemplate, @Value("${crazy.supplier.api.url}") String crazySupplierApiUrl) {
        this.restTemplate = restTemplate;
        this.crazySupplierApiUrl = crazySupplierApiUrl;
    }

    public List<FlightResponseDto> fetchFlights(String departureAirport, String destinationAirport, LocalDate departureDate) {
        // Assuming for one-way search, inboundDate is the same as outboundDate for CrazySupplier API
        SupplierRequest requestDto = new SupplierRequest(
                departureAirport,
                destinationAirport,
                departureDate, //  outboundDate (CET)
                departureDate  // inboundDate (CET) - Assumption arrival on the same day or for one-way segment
        );

        HttpEntity<SupplierRequest> requestEntity = new HttpEntity<>(requestDto);
       log.info("Querying CrazySupplier API: {} with payload: {}", crazySupplierApiUrl, requestDto);

        try {
            ResponseEntity<List<CrazySupplierFlightDto>> response = restTemplate.exchange(
                    crazySupplierApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<CrazySupplierFlightDto>>() {}
            );

            if (response.getBody() != null) {
                return response.getBody().stream()
                        .map(this::mapToFlightResponseDto)
                        .collect(Collectors.toList());
            }
        } catch (RestClientException e) {
            log.error("Error calling CrazySupplier API: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private FlightResponseDto mapToFlightResponseDto(CrazySupplierFlightDto csFlight) {
        return FlightResponseDto.builder()
                .airline(csFlight.getCarrier())
                .supplier(SUPPLIER_NAME)
                .fare(csFlight.getBasePrice().add(csFlight.getTax()))
                .departureAirport(csFlight.getDepartureAirportName())
                .destinationAirport(csFlight.getArrivalAirportName())
                .departureTime(DateTimeUtil.fromCetToUtc(csFlight.getOutboundDateTime()))
                .arrivalTime(DateTimeUtil.fromCetToUtc(csFlight.getInboundDateTime())) // Assuming inboundDateTime is arrival time for the segment
                .build();
    }
}
