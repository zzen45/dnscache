package com.zzeng.dnscache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.repository.DnsCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;

import java.net.InetAddress;

@Service
public class DnsService implements DnsServiceInterface {

    private final DnsCacheRepository dnsCacheRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DnsService(DnsCacheRepository dnsCacheRepository, ObjectMapper objectMapper) {
        this.dnsCacheRepository = dnsCacheRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<DnsRecord> resolveDomain(String domain) {
        return resolveDomain(domain, 300);
    }

    @Override
    public Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, DnsRecord.class));
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(Mono.defer(() -> resolveAndCache(domain, ttlSeconds)));
    }

    @Override
    public Mono<Boolean> exists(String domain) {
        return dnsCacheRepository.get(domain)
                .map(record -> true)
                .defaultIfEmpty(false);
    }

    private Mono<DnsRecord> resolveAndCache(String domain, long ttlSeconds) {
        return Mono.fromCallable(() -> InetAddress.getByName(domain).getHostAddress())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ip -> {
                    DnsRecord record = new DnsRecord(domain, ip, ttlSeconds, false);
                    String json;
                    try {
                        json = objectMapper.writeValueAsString(record);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                    return dnsCacheRepository.set(domain, json, ttlSeconds)
                            .thenReturn(record);
                });
    }

    @Override
    public Flux<DnsRecord> getAllCachedRecords() {
        return dnsCacheRepository.scanKeys()
                .flatMap(key -> dnsCacheRepository.get(key)
                        .flatMap(json -> {
                            try {
                                return Mono.just(objectMapper.readValue(json, DnsRecord.class));
                            } catch (Exception e) {
                                return Mono.empty();
                            }
                        })
                );
    }

    @Override
    public Mono<DnsRecord> getCachedRecord(String domain) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, DnsRecord.class));
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

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

    @Override
    public Mono<DnsRecord> saveManualEntry(DnsRecord record) throws JsonProcessingException {
        record.setManual(true);
        String json = objectMapper.writeValueAsString(record);
        return dnsCacheRepository.set(record.getDomain(), json, record.getTtl())
                .thenReturn(record);
    }
}
