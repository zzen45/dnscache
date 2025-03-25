package com.zzeng.dnscache.dto;

import jakarta.validation.constraints.Min;

public class TtlUpdateRequest {

    @Min(value = 1, message = "TTL must be at least 1 second")
    private long ttl;

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
