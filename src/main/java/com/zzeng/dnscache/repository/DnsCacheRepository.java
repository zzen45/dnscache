package com.zzeng.dnscache.repository;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

/**
 * Repository interface for interacting with Redis DNS cache.
 * Provides basic key-value operations with TTL support.
 */
public interface DnsCacheRepository {

    /**
     * Retrieves a cached value for the given domain.
     *
     * @param domain the domain name (used as Redis key)
     * @return a Mono emitting the cached JSON string, or empty if not found
     */
    Mono<String> get(String domain);

    /**
     * Stores a value in Redis with the given TTL (in seconds).
     *
     * @param domain the domain name (used as key)
     * @param value the JSON string to store
     * @param ttlSeconds time-to-live duration in seconds
     * @return a Mono emitting true when completed
     */
    Mono<Boolean> set(String domain, String value, long ttlSeconds);

    /**
     * Deletes a domain key from Redis.
     *
     * @param domain the domain name to delete
     * @return a Mono emitting true if deletion was successful, false otherwise
     */
    Mono<Boolean> delete(String domain);

    /**
     * Scans and returns all keys currently stored in Redis.
     *
     * @return a Flux emitting each Redis key as a string
     */
    Flux<String> scanKeys();
}
