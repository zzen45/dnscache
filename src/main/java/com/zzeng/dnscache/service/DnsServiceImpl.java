package com.zzeng.dnscache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.config.DnsProperties;
import com.zzeng.dnscache.model.DnsRecord;
import com.zzeng.dnscache.repository.DnsCacheRepository;
import com.zzeng.dnscache.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.InetAddress;
import java.util.List;

@Service
public class DnsServiceImpl implements DnsService {

    private final DnsCacheRepository dnsCacheRepository;
    private final ObjectMapper objectMapper;
    private final long defaultTtl;
    private static final Logger logger = LoggerFactory.getLogger(DnsServiceImpl.class);

    @Autowired
    public DnsServiceImpl(DnsCacheRepository dnsCacheRepository,
                          ObjectMapper objectMapper,
                          DnsProperties dnsProperties) {
        this.dnsCacheRepository = dnsCacheRepository;
        this.objectMapper = objectMapper;
        this.defaultTtl = dnsProperties.getTtl();
    }

    @PostConstruct
    public void init() {
        logger.info("DnsServiceImpl initialized with default TTL: {} seconds", defaultTtl);
    }


    // --- Resolution ---
    @Override
    public Mono<DnsRecord> resolveDomain(String domain) {
        return resolveDomain(domain, defaultTtl);
    }

    @Override
    public Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds) {
        // Check if exists in cache
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                .switchIfEmpty(Mono.defer(() -> resolveAndCache(domain, ttlSeconds)));
    }

    private Mono<DnsRecord> resolveAndCache(String domain, long ttlSeconds) {
        // Actually do the DNS lookup & store
        return Mono.fromCallable(() -> InetAddress.getByName(domain).getHostAddress())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ip -> {
                    DnsRecord record = new DnsRecord(domain, ip, ttlSeconds, false);
                    return JsonUtil.safeSerialize(record, objectMapper)
                            .flatMap(json -> dnsCacheRepository.set(domain, json, ttlSeconds)
                                    .thenReturn(record));
                });
    }

    // --- Cache Create ---
    @Override
    public Mono<DnsRecord> createManualEntry(DnsRecord record) throws JsonProcessingException {
        record.setManual(true);
        return JsonUtil.safeSerialize(record, objectMapper)
                .flatMap(json -> dnsCacheRepository.set(record.getDomain(), json, record.getTtl())
                        .thenReturn(record));
    }

    // --- Cache Read ---
    @Override
    public Mono<DnsRecord> getCachedRecord(String domain) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper));
    }

    @Override
    public Flux<DnsRecord> getAllCachedRecords() {
        return dnsCacheRepository.scanKeys()
                .flatMap(key -> dnsCacheRepository.get(key)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper)));
    }

    @Override
    public Mono<Boolean> exists(String domain) {
        return dnsCacheRepository.get(domain)
                .map(val -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<DnsRecord> getBatch(List<String> domains) {
        return Flux.fromIterable(domains)
                .flatMap(domain -> dnsCacheRepository.get(domain)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper)));
    }

    // --- Cache Update ---
    @Override
    public Mono<Boolean> updateTTL(String domain, long newTTL) {
        return dnsCacheRepository.get(domain)
                .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                .flatMap(record -> {
                    record.setTtl(newTTL);
                    return JsonUtil.safeSerialize(record, objectMapper)
                            .flatMap(serialized -> dnsCacheRepository.set(domain, serialized, newTTL))
                            .map(saved -> true);
                })
                .defaultIfEmpty(false); // domain not found
    }

    // --- Cache Delete ---
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
    public Mono<String> deleteAllManualEntries() {
        return dnsCacheRepository.scanKeys()
                .flatMap(key -> dnsCacheRepository.get(key)
                        .flatMap(json -> JsonUtil.safeDeserialize(json, objectMapper))
                        .flatMap(record -> {
                            if (record.isManual()) {
                                return dnsCacheRepository.delete(key)
                                        .filter(Boolean::booleanValue)
                                        .map(deleted -> 1L);
                            } else {
                                return Mono.just(0L);
                            }
                        })
                )
                .reduce(0L, Long::sum)
                .map(count -> "Deleted " + count + " manual entries.");
    }

    @Override
    public Mono<String> deleteBatch(List<String> domains) {
        return Flux.fromIterable(domains)
                .flatMap(dnsCacheRepository::delete)
                .filter(Boolean::booleanValue)
                .count()
                .map(deletedCount -> "Deleted " + deletedCount + " entries.");
    }
}
