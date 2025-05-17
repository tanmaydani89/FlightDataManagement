package com.aviation.flightdatamanagement.repository;

import com.aviation.flightdatamanagement.entity.FlightDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightDetailsRepository extends JpaRepository<FlightDetails, Long>, JpaSpecificationExecutor<FlightDetails> {
}
