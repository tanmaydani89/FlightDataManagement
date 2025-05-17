package com.aviation.flightdatamanagement.dto;

import lombok.Data;

@Data
public class SearchFlightDetailsRequest {
    private String origin;
    private String destination;
    private String airline;
    private String departureAirport;
    private String arrivalTime;

}
