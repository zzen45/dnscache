package com.zzeng.dnscache.dto;

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

    public String getIp() {
        return ip;
    }

    public long getTtl() {
        return ttl;
    }
}
