package com.zzeng.dnscache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
                .flatMap(ip ->
                        redisTemplate.opsForValue()
                                .set(domain, ip, Duration.ofMinutes(5))
                                .thenReturn(ip)
                );
    }
}
