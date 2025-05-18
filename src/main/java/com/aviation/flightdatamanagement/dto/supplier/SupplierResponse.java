package com.aviation.flightdatamanagement.dto.supplier;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierResponse {

    private String carrier;
    private BigDecimal basePrice;
    private BigDecimal tax;
    private String departureAirportName; // 3-letter airport name
    private String arrivalAirportName;   // 3-letter airport name

    // For parsing from JSON that is LocalDateTime string in CET
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime outboundDateTime; // ISO_LOCAL_DATE_TIME format – CET timezone

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime inboundDateTime;  // ISO_LOCAL_DATE_TIME format – CET timezone (arrival time)
}
