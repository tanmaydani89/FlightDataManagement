package com.aviation.flightdatamanagement.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8090)
@TestPropertySource(properties = {
        "crazy.supplier.api.url=http://localhost:8090/flights"
})
public class FlightControllerSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
    }

    @Test
    void searchFlights_whenCrazySupplierHasMatchingFlights_shouldReturnCombinedResults() throws Exception {
        stubFor(post(urlEqualTo("/flights"))
                .withRequestBody(equalToJson("{\n" +
                        "  \"from\": \"LHR\",\n" +
                        "  \"to\": \"JFK\",\n" +
                        "  \"outboundDate\": \"2024-08-01\",\n" +
                        "  \"inboundDate\": \"2024-08-01\"\n" +
                        "}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("crazy-supplier-LHR-JFK-20240801.json")
                ));

        MvcResult result = mockMvc.perform(get("/api/flights/search")
                        .param("origin", "LHR")
                        .param("destination", "JFK")
                        .param("departureDate", "2024-08-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].supplier", is("CrazySupplier")))
                .andExpect(jsonPath("$[0].airline", is("MockCrazyAir")))
                .andExpect(jsonPath("$[0].departureAirport", is("LHR")))
                .andExpect(jsonPath("$[0].fare").value(495.00))
                .andExpect(jsonPath("$[1].supplier", is("CrazySupplier")))
                .andExpect(jsonPath("$[1].airline", is("MockFastJet")))
                .andReturn();

        // Verify
        verify(1, postRequestedFor(urlEqualTo("/flights"))
                .withRequestBody(equalToJson("{\n" +
                        "  \"from\": \"LHR\",\n" +
                        "  \"to\": \"JFK\",\n" +
                        "  \"outboundDate\": \"2024-08-01\",\n" +
                        "  \"inboundDate\": \"2024-08-01\"\n" +
                        "}")));
    }

    @Test
    void searchFlights_whenCrazySupplierReturnsEmpty_shouldReturnOnlyInternalResults() throws Exception {

        // 1. Setup WireMock stub
        stubFor(post(urlEqualTo("/flights"))
                .withRequestBody(equalToJson("{\n" +
                        "  \"from\": \"CDG\",\n" +
                        "  \"to\": \"AMS\",\n" +
                        "  \"outboundDate\": \"2024-09-15\",\n" +
                        "  \"inboundDate\": \"2024-09-15\"\n" +
                        "}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]") // Empty JSON array
                ));

        // 2. Perform the search request
        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "CDG")
                        .param("destination", "AMS")
                        .param("departureDate", "2024-09-15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // Assuming no internal flights match CDG-AMS

        verify(1, postRequestedFor(urlEqualTo("/flights")));
    }
}
