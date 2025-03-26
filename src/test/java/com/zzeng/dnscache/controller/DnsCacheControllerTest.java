package com.zzeng.dnscache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.dto.DnsRecordCreateRequest;
import com.zzeng.dnscache.dto.DnsRecordResponse;
import com.zzeng.dnscache.service.DnsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = DnsCacheController.class)
@Import(DnsCacheControllerTest.MockConfig.class)
class DnsCacheControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DnsService dnsService; // This will be our mock

    /**
     * A nested TestConfiguration that defines a mock DnsService bean
     * which Spring will inject into the controller.
     */
    static class MockConfig {
        @Bean
        DnsService dnsService() {
            return Mockito.mock(DnsService.class);
        }
    }

    @Test
    void testCreateManualEntry() throws JsonProcessingException {
        // 1) Define input request
        DnsRecordCreateRequest request = new DnsRecordCreateRequest();
        request.setDomain("test.com");
        request.setIp("127.0.0.1");
        request.setTtl(300L);

        // 2) Define the mock response from the service
        DnsRecordResponse mockResponse = new DnsRecordResponse();
        mockResponse.setDomain("test.com");
        mockResponse.setIp("127.0.0.1");
        mockResponse.setTtl(300L);

        // 3) Tell the mock service how to respond
        when(dnsService.createManualEntry(any()))
                .thenReturn(Mono.just(mockResponse));

        // 4) Call the endpoint via WebTestClient and verify
        webTestClient.post()
                .uri("/api/dns/cache")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DnsRecordResponse.class)
                .isEqualTo(mockResponse);
    }

    // Add more tests for GET, PATCH, DELETE, etc.
}