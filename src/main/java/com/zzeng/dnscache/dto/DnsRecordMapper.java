package com.zzeng.dnscache.dto;

import com.zzeng.dnscache.model.DnsRecord;

public class DnsRecordMapper {

    public static DnsRecordResponse toResponse(DnsRecord record) {
        return new DnsRecordResponse(
                record.getDomain(),
                record.getIp(),
                record.getTtl()
        );
    }
}
