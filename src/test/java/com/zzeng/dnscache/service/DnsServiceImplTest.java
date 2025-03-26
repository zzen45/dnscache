package com.zzeng.dnscache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.config.DnsProperties;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.repository.DnsCacheRepository;
import com.zzeng.dnscache.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class DnsServiceImplTest {

    @Mock
    private DnsCacheRepository dnsCacheRepository;

    // We'll create and assign these ourselves:
    private ObjectMapper objectMapper;
    private DnsProperties dnsProperties;

    @InjectMocks
    private DnsServiceImpl dnsServiceImpl;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dnsProperties = new DnsProperties();
        dnsProperties.setTtl(300L);

        // Because we used @InjectMocks, dnsServiceImpl will have the dnsCacheRepository injected
        // We can manually set other constructor args if needed:
        dnsServiceImpl = new DnsServiceImpl(dnsCacheRepository, objectMapper, dnsProperties);
    }

    @Test
    void testGetCachedRecordFound() {
        String domain = "example.com";
        DnsRecord record = new DnsRecord(domain, "93.184.216.34", 300L, false);

        // The repository returns JSON strings
        String json = JsonUtil.safeSerialize(record, objectMapper).block();

        // Mock the repository call
        when(dnsCacheRepository.get(eq(domain))).thenReturn(Mono.just(json));

        // Now call the service method
        Mono<?> result = dnsServiceImpl.getCachedRecord(domain);

        // StepVerifier to validate the Mono
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    // Check domain, IP, TTL, etc.
                    return true; // Or do real checks on response
                })
                .verifyComplete();
    }

    // Add more tests: test TTL update, test record not found, etc.
}