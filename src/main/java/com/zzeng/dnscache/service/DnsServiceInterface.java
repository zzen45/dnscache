package com.zzeng.dnscache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzeng.dnscache.model.DnsRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface for DNS resolution and smart caching functionality.
 * Provides operations to resolve, create, read, update, and delete DNS records,
 * with support for manual overrides, TTL management, and batch processing.
 */
public interface DnsServiceInterface {

    // --- Resolution ---
    /**
     * Resolves a domain name using the default TTL configured in the application.
     * If the domain exists in Redis cache, the cached result is returned.
     * Otherwise, a DNS lookup is performed and the result is cached.
     *
     * @param domain the domain name to resolve (e.g., "example.com")
     * @return a Mono emitting the resolved {@link DnsRecord}
     */
    Mono<DnsRecord> resolveDomain(String domain);

    /**
     * Resolves a domain name using a custom TTL provided at runtime.
     * If the domain exists in Redis cache, the cached result is returned.
     * Otherwise, a DNS lookup is performed and the result is cached with the specified TTL.
     *
     * @param domain the domain name to resolve
     * @param ttlSeconds time-to-live in seconds for the cached result
     * @return a Mono emitting the resolved {@link DnsRecord}
     */
    Mono<DnsRecord> resolveDomain(String domain, long ttlSeconds);

    // --- Cache Create ---
    /**
     * Manually inserts or overrides a DNS record in the Redis cache.
     * Marks the record as "manual" and uses the TTL provided in the request.
     *
     * @param record the {@link DnsRecord} to store
     * @return a Mono emitting the saved {@link DnsRecord}
     * @throws JsonProcessingException if serialization to JSON fails
     */
    Mono<DnsRecord> createManualEntry(DnsRecord record) throws JsonProcessingException;

    // --- Cache Read ---
    /**
     * Retrieves a cached DNS record for a specific domain.
     *
     * @param domain the domain name to retrieve
     * @return a Mono emitting the {@link DnsRecord}, or empty if not found
     */
    Mono<DnsRecord> getCachedRecord(String domain);

    /**
     * Retrieves all cached DNS records from Redis.
     *
     * @return a Flux stream of {@link DnsRecord} objects
     */
    Flux<DnsRecord> getAllCachedRecords();

    /**
     * Checks whether a domain exists in the Redis cache.
     *
     * @param domain the domain name to check
     * @return a Mono emitting true if the domain exists in cache, false otherwise
     */
    Mono<Boolean> exists(String domain);

    /**
     * Performs a batch read of multiple domain names.
     * Returns only records that are currently in the Redis cache.
     *
     * @param domains list of domain names to retrieve
     * @return a Flux stream of found {@link DnsRecord} entries
     */
    Flux<DnsRecord> getBatch(List<String> domains); // for batch reads

    // --- Cache Update ---
    /**
     * Updates the TTL (time-to-live) for an existing DNS record in the cache.
     *
     * @param domain the domain whose TTL should be updated
     * @param newTTL the new TTL value in seconds
     * @return a Mono emitting true if the update was successful, false if the record was not found
     */
    Mono<Boolean> updateTTL(String domain, long newTTL);

    // --- Cache Delete ---
    /**
     * Deletes a specific domain from the Redis cache.
     *
     * @param domain the domain name to delete
     * @return a Mono emitting true if deletion succeeded, false if not found
     */
    Mono<Boolean> deleteCachedRecord(String domain);

    /**
     * Clears all cached DNS entries from Redis.
     *
     * @return a Mono emitting a status message
     */
    Mono<String> clearCache();

    /**
     * Deletes all DNS entries that were manually created (isManual = true).
     * This does not affect automatically resolved entries.
     *
     * @return a Mono emitting a summary of how many entries were deleted
     */
    Mono<String> deleteAllManualEntries();

    /**
     * Deletes a batch of specific domains from the Redis cache.
     *
     * @param domains list of domain names to delete
     * @return a Mono emitting a summary of how many entries were deleted
     */
    Mono<String> deleteBatch(List<String> domains);
}
