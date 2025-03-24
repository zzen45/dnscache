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
public class DnsService {

    private final DnsCacheRepository dnsCacheRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DnsService(DnsCacheRepository dnsCacheRepository, ObjectMapper objectMapper) {
        this.dnsCacheRepository = dnsCacheRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Resolves a domain name using cache or DNS lookup.
     * - If the domain is cached, return it.
     * - If not, resolve and cache it, then return the IP.
     */
    public Mono<DnsRecord> resolveDomain(String domain) {
        return resolveDomain(domain, 300); // fallback to default TTL
    }
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

    /**
     * Performs actual DNS lookup and stores the result in Redis with a TTL.
     * TTL is currently set to 5 minutes.
     */
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

    /**
     * Returns a stream of all cached domain-IP entries in Redis.
     * Each entry is returned as a Map.Entry<String, String>.
     */
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

    /**
     * Returns a cached IP for a given domain if exists in Redis.
     */
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

    /**
     * Delete a domain from Redis cache.
     */
    public Mono<Boolean> deleteCachedRecord(String domain) {
        return dnsCacheRepository.delete(domain);
    }

    /**
     * Clears all keys from Redis cache.
     */
    public Mono<String> clearCache() {
        return dnsCacheRepository.scanKeys()
                .flatMap(dnsCacheRepository::delete)
                .then(Mono.just("Cache cleared"));
    }

    /**
     * Manually insert or override a DNS entry.
     */
    public Mono<DnsRecord> saveManualEntry(String domain, String ip, long ttlSeconds) throws JsonProcessingException {
        DnsRecord record = new DnsRecord(domain, ip, ttlSeconds, true);
        String json = objectMapper.writeValueAsString(record);

        return dnsCacheRepository.set(domain, json, ttlSeconds)
                .thenReturn(record);
    }

}
