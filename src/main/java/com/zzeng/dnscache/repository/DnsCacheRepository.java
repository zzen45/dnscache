package com.zzeng.dnscache.repository;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface DnsCacheRepository {
    Mono<String> get(String domain);
    Mono<Boolean> set(String domain, String value, long ttlSeconds);
    Mono<Boolean> delete(String domain);
    Flux<String> scanKeys();
}
