package com.zzeng.dnscache.model;

public class DnsRecord {

    private String domain;

    private String ip;

    private long ttl;

    private boolean isManual;

    public DnsRecord() {}

    public DnsRecord(String domain, String ip, long ttl, boolean isManual) {
        this.domain = domain;
        this.ip = ip;
        this.ttl = ttl;
        this.isManual = isManual;
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

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean Manual) {
        isManual = Manual;
    }
}
