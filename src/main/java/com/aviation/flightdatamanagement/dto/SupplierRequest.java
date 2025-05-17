package com.aviation.flightdatamanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    private String from; // 3-letter airport code
    private String to;   // 3-letter airport code
    private LocalDate outboundDate; // ISO_LOCAL_DATE format – CET timezone
    private LocalDate inboundDate;  // ISO_LOCAL_DATE format – CET timezone (arrival date for the segment)
}
