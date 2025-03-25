package com.zzeng.dnscache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.dto.*;
import com.zzeng.dnscache.service.DnsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dns")
public class DnsCacheController {

    private final DnsService dnsService;

    @Autowired
    public DnsCacheController(DnsService dnsService) {
        this.dnsService = dnsService;
    }


    // --- Resolution ---
    @GetMapping("/resolve")
    public Mono<DnsRecordResponse> resolveDomain(@RequestParam String domain,
                                                 @RequestParam(required = false) Long ttl) {
        return dnsService.resolveDomain(domain, ttl);
    }


    // --- Create ---
    @PostMapping("/cache")
    public Mono<DnsRecordResponse> createManualEntry(@Valid @RequestBody DnsRecordCreateRequest request)
            throws JsonProcessingException {
        return dnsService.createManualEntry(DnsRecordMapper.toManualDnsRecord(request));
    }


    // --- Read ---
    @GetMapping("/cache/{domain}")
    public Mono<DnsRecordResponse> getCachedRecord(@PathVariable String domain) {
        return dnsService.getCachedRecord(domain);
    }

    @GetMapping("/cache")
    public Flux<DnsRecordResponse> getAllCachedRecords() {
        return dnsService.getAllCachedRecords();
    }

    @GetMapping("/cache/exists/{domain}")
    public Mono<Boolean> exists(@PathVariable String domain) {
        return dnsService.exists(domain);
    }

    @PostMapping("/cache/batch")
    public Flux<DnsRecordResponse> getBatchRecords(@Valid @RequestBody DnsBatchRequest request) {
        return dnsService.getBatch(request.getDomains());
    }


    // --- Update ---
    @PatchMapping("/cache/{domain}/ttl")
    public Mono<Boolean> updateTTL(@PathVariable String domain,
                                   @Valid @RequestBody TtlUpdateRequest request) {
        return dnsService.updateTTL(domain, request.getTtl());
    }


    // --- Delete ---
    @DeleteMapping("/cache/{domain}")
    public Mono<Boolean> deleteCachedRecord(@PathVariable String domain) {
        return dnsService.deleteCachedRecord(domain);
    }

    @DeleteMapping("/cache")
    public Mono<String> clearCache() {
        return dnsService.clearCache();
    }

    @DeleteMapping("/cache/manual")
    public Mono<String> deleteAllManualEntries() {
        return dnsService.deleteAllManualEntries();
    }

    @DeleteMapping("/cache/batch")
    public Mono<String> deleteBatch(@Valid @RequestBody DnsBatchRequest request) {
        return dnsService.deleteBatch(request.getDomains());
    }
}
