package com.zzeng.dnscache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DnsServiceInterface {

    // ---------------------------
    // Resolution
    // ---------------------------
    Mono<DnsRecord> resolveDomain(String domain);
    Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds);

    // ---------------------------
    // Cache Create
    // ---------------------------
    Mono<DnsRecord> saveManualEntry(DnsRecord record) throws JsonProcessingException;

    // ---------------------------
    // Cache Read
    // ---------------------------
    Mono<DnsRecord> getCachedRecord(String domain);
    Flux<DnsRecord> getAllCachedRecords();
    Mono<Boolean> exists(String domain);
    Flux<DnsRecord> getBatch(List<String> domains); // for batch reads

    // ---------------------------
    // Cache Update
    // ---------------------------
    Mono<Boolean> updateTTL(String domain, long newTTL);

    // ---------------------------
    // Cache Delete
    // ---------------------------
    Mono<Boolean> deleteCachedRecord(String domain);
    Mono<String> clearCache();
    Mono<String> deleteManualEntries();
    Mono<String> deleteBatch(List<String> domains);
}
