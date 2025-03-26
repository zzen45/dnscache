package com.zzeng.dnscache.util;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.net.UnknownHostException;

/**
 * A helper class using dnsjava to resolve a domain with a specific DNS server.
 */
public class DnsFallbackResolver {

    /**
     * Attempts to resolve the A record for the given domain using the specified DNS server (e.g., "8.8.8.8").
     *
     * @param domain the domain to resolve
     * @param dnsServer the DNS server IP to query
     * @return the IPv4 address as a string
     * @throws UnknownHostException if resolution fails
     */
    public static String resolve(String domain, String dnsServer) throws UnknownHostException {
        try {
            Lookup lookup = new Lookup(domain, Type.A);
            SimpleResolver resolver = new SimpleResolver(dnsServer);
            lookup.setResolver(resolver);

            Record[] records = lookup.run();

            if (records == null || records.length == 0) {
                throw new UnknownHostException("No A record found for " + domain + " using " + dnsServer);
            }

            ARecord a = (ARecord) records[0];
            return a.getAddress().getHostAddress();
        } catch (Exception e) {
            throw new UnknownHostException("Failed to resolve domain " + domain + " with server " + dnsServer);
        }
    }
}
