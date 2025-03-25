package com.zzeng.dnscache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.service.DnsService;
import com.zzeng.dnscache.dto.DnsRecordCreateRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public Mono<DnsRecord> resolveDomain(@RequestParam String domain,
                                         @RequestParam(required = false) Long ttl) {
        if (ttl == null) {
            return dnsService.resolveDomain(domain);
        } else {
            return dnsService.resolveDomain(domain, ttl);
        }
    }

    // --- Cache Create ---
    @PostMapping("/cache")
    public Mono<DnsRecord> createManualEntry(@Valid @RequestBody DnsRecordCreateRequest request) throws JsonProcessingException {
        DnsRecord record = new DnsRecord(
                request.getDomain(),
                request.getIp(),
                request.getTtl(),
                true
        );
        return dnsService.createManualEntry(record);
    }

    // --- Cache Read ---
    @GetMapping("/cache/{domain}")
    public Mono<DnsRecord> getCachedRecord(@PathVariable String domain) {
        return dnsService.getCachedRecord(domain);
    }

    @GetMapping("/cache")
    public Flux<DnsRecord> getAllCachedRecords() {
        return dnsService.getAllCachedRecords();
    }

    @GetMapping("/cache/exists/{domain}")
    public Mono<Boolean> exists(@PathVariable String domain) {
        return dnsService.exists(domain);
    }

    @PostMapping("/cache/batch")
    public Flux<DnsRecord> getBatchRecords(@RequestBody List<String> domains) {
        return dnsService.getBatch(domains);
    }

    // --- Cache Update ---
    @PatchMapping("/cache/{domain}/ttl")
    public Mono<Boolean> updateTTL(@PathVariable String domain,
                                   @RequestParam long ttl) {
        return dnsService.updateTTL(domain, ttl);
    }

    // --- Cache Delete ---
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
    public Mono<String> deleteBatch(@RequestBody List<String> domains) {
        return dnsService.deleteBatch(domains);
    }
}
