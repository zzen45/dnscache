package com.zzeng.dnscache.dto;

import java.util.Objects;

public class DnsRecordResponse {

    private String domain;
    private String ip;
    private long ttl;

    public DnsRecordResponse() {}

    public DnsRecordResponse(String domain, String ip, long ttl) {
        this.domain = domain;
        this.ip = ip;
        this.ttl = ttl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DnsRecordResponse that)) return false;
        return ttl == that.ttl &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, ip, ttl);
    }

}