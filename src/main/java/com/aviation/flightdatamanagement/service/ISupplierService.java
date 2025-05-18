package com.aviation.flightdatamanagement.service;

import com.aviation.flightdatamanagement.dto.FlightResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface ISupplierService {

    //Keeping it open in case multiple suppliers are integrated they will also implement this interface

    List<FlightResponseDto> fetchFlights(String departureAirport, String destinationAirport, LocalDate departureDate);
}
