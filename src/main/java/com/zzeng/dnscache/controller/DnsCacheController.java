package com.zzeng.dnscache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.service.DnsServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/dns")
public class DnsCacheController {

    private final DnsServiceInterface dnsService;

    @Autowired
    public DnsCacheController(DnsServiceInterface dnsService) {
        this.dnsService = dnsService;
    }

    // --- Resolution ---
    // GET /resolve?domain=example.com[&ttl=150]
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
    // POST /cache
    @PostMapping("/cache")
    public Mono<DnsRecord> createManualEntry(@RequestBody DnsRecord record) throws JsonProcessingException {
        return dnsService.createManualEntry(record);
    }

    // --- Cache Read ---
    // GET /cache/{domain}
    @GetMapping("/cache/{domain}")
    public Mono<DnsRecord> getCachedRecord(@PathVariable String domain) {
        return dnsService.getCachedRecord(domain);
    }

    // GET /cache
    @GetMapping("/cache")
    public Flux<DnsRecord> getAllCachedRecords() {
        return dnsService.getAllCachedRecords();
    }

    // GET /cache/exists/{domain}
    @GetMapping("/cache/exists/{domain}")
    public Mono<Boolean> exists(@PathVariable String domain) {
        return dnsService.exists(domain);
    }

    // POST /cache/batch
    @PostMapping("/cache/batch")
    public Flux<DnsRecord> getBatchRecords(@RequestBody List<String> domains) {
        return dnsService.getBatch(domains);
    }

    // --- Cache Update ---
    // PATCH /cache/{domain}/ttl?ttl=600
    @PatchMapping("/cache/{domain}/ttl")
    public Mono<Boolean> updateTTL(@PathVariable String domain,
                                   @RequestParam long ttl) {
        return dnsService.updateTTL(domain, ttl);
    }

    // --- Cache Delete ---
    // DELETE /cache/{domain}
    @DeleteMapping("/cache/{domain}")
    public Mono<Boolean> deleteCachedRecord(@PathVariable String domain) {
        return dnsService.deleteCachedRecord(domain);
    }

    // DELETE /cache
    @DeleteMapping("/cache")
    public Mono<String> clearCache() {
        return dnsService.clearCache();
    }

    // DELETE /cache/manual
    @DeleteMapping("/cache/manual")
    public Mono<String> deleteAllManualEntries() {
        return dnsService.deleteAllManualEntries();
    }

    // DELETE /cache/batch
    @DeleteMapping("/cache/batch")
    public Mono<String> deleteBatch(@RequestBody List<String> domains) {
        return dnsService.deleteBatch(domains);
    }
}
