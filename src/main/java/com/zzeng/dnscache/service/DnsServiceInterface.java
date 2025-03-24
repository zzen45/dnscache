package com.zzeng.dnscache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DnsServiceInterface {

    Mono<DnsRecord> resolveDomain(String domain);

    Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds);

    Flux<DnsRecord> getAllCachedRecords();

    Mono<DnsRecord> getCachedRecord(String domain);

    Mono<Boolean> deleteCachedRecord(String domain);

    Mono<String> clearCache();

    Mono<DnsRecord> saveManualEntry(String domain, String ip, long ttlSeconds) throws JsonProcessingException;

}
