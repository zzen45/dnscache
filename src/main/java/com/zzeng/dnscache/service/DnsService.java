package com.zzeng.dnscache.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.model.DnsRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import java.net.InetAddress;
import java.time.Duration;


@Service
public class DnsService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DnsService(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
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
        return redisTemplate.opsForValue().get(domain)
                .flatMap(json -> {
                    try {
                        return Mono.just(new ObjectMapper().readValue(json, DnsRecord.class));
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
                        json = new ObjectMapper().writeValueAsString(record);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                    return redisTemplate.opsForValue()
                            .set(domain, json, Duration.ofSeconds(ttlSeconds))
                            .thenReturn(record);
                });
    }

    /**
     * Returns a stream of all cached domain-IP entries in Redis.
     * Each entry is returned as a Map.Entry<String, String>.
     */
    public Flux<DnsRecord> getAllCachedRecords() {
        return redisTemplate.scan()
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .flatMap(json -> {
                            try {
                                DnsRecord record = objectMapper.readValue(json, DnsRecord.class);
                                return Mono.just(record);
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
        return redisTemplate.opsForValue().get(domain)
                .flatMap(json -> {
                    try {
                        return Mono.just(new ObjectMapper().readValue(json, DnsRecord.class));
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Delete a domain from Redis cache.
     */
    public Mono<Boolean> deleteCachedRecord(String domain) {
        return redisTemplate.delete(domain).map(count -> count > 0);
    }

    /**
     * Clears all keys from Redis cache.
     */
    public Mono<String> clearCache() {
        return redisTemplate.scan()  // scan all
                .collectList()  // all -> list
                .flatMapMany(Flux::fromIterable)  // list -> stream
                .flatMap(redisTemplate::delete)  // delete each key
                .then(Mono.just("Cache cleared"));
    }

    /**
     * Manually insert or override a DNS entry.
     */
    public Mono<DnsRecord> saveManualEntry(String domain, String ip, long ttlSeconds) throws JsonProcessingException {
        DnsRecord record = new DnsRecord(domain, ip, ttlSeconds, true);
        String json = objectMapper.writeValueAsString(record);

        return redisTemplate.opsForValue()
                .set(domain, json, Duration.ofSeconds(ttlSeconds))
                .thenReturn(record);
    }

}
