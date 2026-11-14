package com.nexus.common.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class UrlSecurityValidator {

    private UrlSecurityValidator() {}

    /**
     * Validates an external URL for SSRF protection.
     * Rejects invalid schemes, loopback, link-local, and private addresses.
     */
    public static void validateExternalUrl(String urlString, boolean allowPrivateIps) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("Target URL cannot be empty");
        }

        URI uri;
        try {
            uri = URI.create(urlString.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + urlString, e);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only HTTP and HTTPS protocols are allowed: " + scheme);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Target URL host cannot be empty");
        }

        if (!allowPrivateIps) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress address : addresses) {
                    if (isRestrictedIp(address)) {
                        throw new SecurityException("Access to internal, loopback, or private network address is forbidden: " + host + " (" + address.getHostAddress() + ")");
                    }
                }
            } catch (UnknownHostException e) {
                // Host resolution failure
                throw new IllegalArgumentException("Could not resolve host: " + host, e);
            }
        }
    }

    private static boolean isRestrictedIp(InetAddress address) {
        return address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || address.isAnyLocalAddress();
    }
}
