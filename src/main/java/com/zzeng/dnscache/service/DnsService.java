package com.zzeng.dnscache.service;

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

    public Mono<String> resolveDomain(String domain) {
        return redisTemplate.opsForValue().get(domain)
                .switchIfEmpty(Mono.defer(() -> resolveAndCache(domain)));
    }

    private Mono<String> resolveAndCache(String domain) {
        return Mono.fromCallable(() -> {
                    InetAddress address = InetAddress.getByName(domain);
                    return address.getHostAddress();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ip ->
                        redisTemplate.opsForValue()
                                .set(domain, ip, Duration.ofMinutes(5))
                                .thenReturn(ip)
                );
    }

    public Flux<Map.Entry<String, String>> getAllCachedRecords() {
        return redisTemplate.scan()
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .map(value -> Map.entry(key, value))
                );
    }

    public Mono<String> getCachedRecord(String domain) {
        return redisTemplate.opsForValue().get(domain);
    }

    public Mono<Boolean> deleteCachedRecord(String domain) {
        return redisTemplate.delete(domain).map(count -> count > 0);
    }

    public Mono<String> clearCache() {
        return redisTemplate.scan()
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .flatMap(redisTemplate::delete)
                .then(Mono.just("Cache cleared"));
    }

}
