package com.zzeng.dnscache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "dns")
public class DnsProperties {

    private long ttl = 300;

    private List<String> fallbackServers;

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public List<String> getFallbackServers() {
        return fallbackServers;
    }

    public void setFallbackServers(List<String> fallbackServers) {
        this.fallbackServers = fallbackServers;
    }
}