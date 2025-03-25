package com.zzeng.dnscache.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzeng.dnscache.model.DnsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private JsonUtil() {}

    public static Mono<DnsRecord> safeDeserialize(String json, ObjectMapper objectMapper) {
        try {
            DnsRecord record = objectMapper.readValue(json, DnsRecord.class);
            return Mono.just(record);
        } catch (Exception e) {
            logger.warn("Failed to deserialize JSON: {}", json, e);
            return Mono.empty();
        }
    }

    public static Mono<String> safeSerialize(DnsRecord record, ObjectMapper objectMapper) {
        try {
            return Mono.just(objectMapper.writeValueAsString(record));
        } catch (Exception e) {
            logger.error("Failed to serialize DnsRecord: {}", record, e);
            return Mono.error(e);
        }
    }
}
