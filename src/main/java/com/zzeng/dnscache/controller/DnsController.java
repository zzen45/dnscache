package com.zzeng.dnscache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsManualEntryRequest;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.service.DnsServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/dns")
public class DnsController {
    private final DnsServiceInterface dnsService;

    @Autowired
    public DnsController(DnsServiceInterface dnsService) {
        this.dnsService = dnsService;
    }


    // Test using -> http://localhost:8080/api/dns
    // {domain} = example.com

    // GET /resolve?domain=example.com
    // → Returns IP address (from cache or resolved)
    @GetMapping("/resolve")
    public Mono<DnsRecord> resolve(@RequestParam String domain,
                                   @RequestParam(required = false, defaultValue = "300") long ttl) {
        return dnsService.resolveDomain(domain, ttl);
    }


    // GET /cache/{domain}
    // → Returns cached IP for domain (if exists)
    @GetMapping("/cache/{domain}")
    public Mono<DnsRecord> getCached(@PathVariable String domain) {
        return dnsService.getCachedRecord(domain);
    }

    // GET /cache
    // → Returns all cached domain-IP pairs
    @GetMapping("/cache")
    public Flux<DnsRecord> getAllCached() {
        return dnsService.getAllCachedRecords();
    }

    // DELETE /cache/{domain}
    // → Deletes a specific cached domain
    @DeleteMapping("/cache/{domain}")
    public Mono<Boolean> deleteCached(@PathVariable String domain) {
        return dnsService.deleteCachedRecord(domain);
    }

    // DELETE /cache
    // → Clears entire cache
    @DeleteMapping("/cache")
    public Mono<String> clearCache() {
        return dnsService.clearCache();
    }

    // POST /cache
    // → Manually insert or override a DNS entry
    @PostMapping("/cache")
    public Mono<DnsRecord> addManualEntry(@RequestBody DnsManualEntryRequest request) throws JsonProcessingException {
        return dnsService.saveManualEntry(
                request.getDomain(),
                request.getIp(),
                request.getTtl()
        );
    }

}

