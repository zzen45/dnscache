package com.zzeng.dnscache.service;

import com.zzeng.dnscache.model.DnsRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import java.util.Map;
import java.net.InetAddress;
import java.time.Duration;

@Service
public class DnsService {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    public DnsService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Resolves a domain name using cache or DNS lookup.
     * - If the domain is cached, return it.
     * - If not, resolve and cache it, then return the IP.
     */
    public Mono<String> resolveDomain(String domain) {
        return resolveDomain(domain, 300); // fallback to default TTL
    }
    public Mono<String> resolveDomain(String domain, long ttlSeconds) {
        return redisTemplate.opsForValue().get(domain)
                .switchIfEmpty(Mono.defer(() -> resolveAndCache(domain, ttlSeconds)));
    }

    /**
     * Performs actual DNS lookup and stores the result in Redis with a TTL.
     * TTL is currently set to 5 minutes.
     */
    private Mono<String> resolveAndCache(String domain, long ttlSeconds) {
        return Mono.fromCallable(() -> InetAddress.getByName(domain).getHostAddress())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ip ->
                        redisTemplate.opsForValue()
                                .set(domain, ip, Duration.ofSeconds(ttlSeconds))
                                .thenReturn(ip)
                );
    }

    /**
     * Returns a stream of all cached domain-IP entries in Redis.
     * Each entry is returned as a Map.Entry<String, String>.
     */
    public Flux<DnsRecord> getAllCachedRecords() {
        return redisTemplate.scan()
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .map(ip -> new DnsRecord(key, ip, 300, false)) // placeholder TTL & manual flag
                );
    }

    /**
     * Returns a cached IP for a given domain if exists in Redis.
     */
    public Mono<String> getCachedRecord(String domain) {
        return redisTemplate.opsForValue().get(domain);
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

}
