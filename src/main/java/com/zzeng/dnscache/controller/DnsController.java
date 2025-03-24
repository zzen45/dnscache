package com.zzeng.dnscache.controller;

import com.zzeng.dnscache.service.DnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.Map;

@RestController
@RequestMapping("/api/dns")
public class DnsController {
    private final DnsService dnsService;

    @Autowired
    public DnsController(DnsService dnsService) {
        this.dnsService = dnsService;
    }

    @GetMapping("/resolve")
    public Mono<String> resolve(@RequestParam String domain) {
        return dnsService.resolveDomain(domain);
    }

    @GetMapping("/cache/{domain}")
    public Mono<String> getCached(@PathVariable String domain) {
        return dnsService.getCachedRecord(domain);
    }

    @GetMapping("/cache")
    public Flux<Map.Entry<String, String>> getAllCached() {
        return dnsService.getAllCachedRecords();
    }

    @DeleteMapping("/cache/{domain}")
    public Mono<Boolean> deleteCached(@PathVariable String domain) {
        return dnsService.deleteCachedRecord(domain);
    }

    @DeleteMapping("/cache")
    public Mono<String> clearCache() {
        return dnsService.clearCache();
    }

}

