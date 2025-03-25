package com.zzeng.dnscache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.config.DnsProperties;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.repository.DnsCacheRepository;
import com.zzeng.dnscache.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;

import java.net.InetAddress;

@Service
public class DnsService implements DnsServiceInterface {

    // Dependencies
    private final DnsCacheRepository dnsCacheRepository;
    private final ObjectMapper objectMapper;
    private final long defaultTtl;

    @Autowired
    public DnsService(DnsCacheRepository dnsCacheRepository, ObjectMapper objectMapper, DnsProperties dnsProperties) {
        this.dnsCacheRepository = dnsCacheRepository;
        this.objectMapper = objectMapper;
        this.defaultTtl = dnsProperties.getTtl();
    }


    // Resolution
    @Override
    public Mono<DnsRecord> resolveDomain(String domain) {
        return resolveDomain(domain, defaultTtl);
    }

    @Override
    public Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                .switchIfEmpty(Mono.defer(() -> resolveAndCache(domain, ttlSeconds)));
    }

    private Mono<DnsRecord> resolveAndCache(String domain, long ttlSeconds) {
        return Mono.fromCallable(() -> InetAddress.getByName(domain).getHostAddress())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ip -> {
                    DnsRecord record = new DnsRecord(domain, ip, ttlSeconds, false);
                    return JsonUtil.safeSerialize(record, objectMapper)
                            .flatMap(json -> dnsCacheRepository.set(domain, json, ttlSeconds)
                                    .thenReturn(record));
                });
    }


    // Cache Reads
    @Override
    public Flux<DnsRecord> getAllCachedRecords() {
        return dnsCacheRepository.scanKeys()
                .flatMap(key -> dnsCacheRepository.get(key)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper)));
    }

    @Override
    public Mono<DnsRecord> getCachedRecord(String domain) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper));
    }

    @Override
    public Mono<Boolean> exists(String domain) {
        return dnsCacheRepository.get(domain)
                .map(record -> true)
                .defaultIfEmpty(false);
    }


    // Cache Writes
    @Override
    public Mono<DnsRecord> saveManualEntry(DnsRecord record) {
        record.setManual(true);
        return JsonUtil.safeSerialize(record, objectMapper)
                .flatMap(json -> dnsCacheRepository.set(record.getDomain(), json, record.getTtl())
                        .thenReturn(record));
    }


    // Cache Deletes
    @Override
    public Mono<Boolean> deleteCachedRecord(String domain) {
        return dnsCacheRepository.delete(domain);
    }

    @Override
    public Mono<String> clearCache() {
        return dnsCacheRepository.scanKeys()
                .flatMap(dnsCacheRepository::delete)
                .then(Mono.just("Cache cleared"));
    }

}
