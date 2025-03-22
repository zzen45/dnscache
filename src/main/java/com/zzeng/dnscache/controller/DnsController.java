package com.zzeng.dnscache.controller;

import com.zzeng.dnscache.service.DnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
}
