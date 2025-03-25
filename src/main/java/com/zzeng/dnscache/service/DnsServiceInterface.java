package com.zzeng.dnscache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DnsServiceInterface {

    // Resolution
    Mono<DnsRecord> resolveDomain(String domain);
    Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds);

    // Cache read
    Mono<DnsRecord> getCachedRecord(String domain);
    Flux<DnsRecord> getAllCachedRecords();
    Mono<Boolean> exists(String domain);

    // Cache write
    Mono<DnsRecord> saveManualEntry(DnsRecord record);

    // Cache delete
    Mono<Boolean> deleteCachedRecord(String domain);
    Mono<String> clearCache();
}
