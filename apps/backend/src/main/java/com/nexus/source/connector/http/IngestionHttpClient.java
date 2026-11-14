package com.nexus.source.connector.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class IngestionHttpClient {

    private static final Logger log = LoggerFactory.getLogger(IngestionHttpClient.class);

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int MAX_RESPONSE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final String USER_AGENT = "NEXUS-Scout/1.0 (+https://github.com/nexus)";

    private static final Set<String> BLOCKED_HOST_SUFFIXES = Set.of(
            ".localhost", ".local", ".internal", ".corp", ".home"
    );

    /**
     * Executes an authenticated or public GET request with SSRF guardrails and size limits.
     */
    public String get(String urlString) throws Exception {
        return getWithRetry(urlString, 2);
    }

    private String getWithRetry(String urlString, int maxRetries) throws Exception {
        validateUrlSafety(urlString);

        int attempts = 0;
        long backoffMs = 500;

        while (true) {
            attempts++;
            HttpURLConnection conn = null;
            try {
                URL url = new URI(urlString).toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setInstanceFollowRedirects(false); // Manually validate redirects to prevent SSRF
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setRequestProperty("Accept", "application/json, text/plain, */*");

                int statusCode = conn.getResponseCode();

                // Handle Redirects safely
                if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String redirectUrl = conn.getHeaderField("Location");
                    if (redirectUrl != null) {
                        validateUrlSafety(redirectUrl);
                        return getWithRetry(redirectUrl, maxRetries - 1);
                    }
                }

                // Handle Rate Limiting & Transient Server Errors with backoff
                if ((statusCode == 429 || statusCode >= 500) && attempts <= maxRetries) {
                    log.warn("HTTP {} from {}, backing off for {}ms (attempt {}/{})", statusCode, urlString, backoffMs, attempts, maxRetries);
                    Thread.sleep(backoffMs);
                    backoffMs *= 2;
                    continue;
                }

                if (statusCode < 200 || statusCode >= 300) {
                    throw new RuntimeException("HTTP GET failed with status code " + statusCode + " from: " + urlString);
                }

                // Stream response with max size protection
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    int totalBytes = 0;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                        if (totalBytes > MAX_RESPONSE_BYTES) {
                            throw new SecurityException("Response exceeded maximum permitted size of 5MB");
                        }
                        out.write(buffer, 0, bytesRead);
                    }
                    return out.toString(StandardCharsets.UTF_8);
                }

            } catch (Exception e) {
                if (attempts > maxRetries) {
                    throw e;
                }
                log.warn("Network error fetching {}, retrying: {}", urlString, e.getMessage());
                Thread.sleep(backoffMs);
                backoffMs *= 2;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    /**
     * Validates that the URL uses HTTPS and resolves to public IP addresses only.
     */
    public void validateUrlSafety(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("Target URL cannot be null or blank");
        }

        URI uri;
        try {
            uri = URI.create(urlString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed URL: " + urlString);
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new SecurityException("Strictly HTTPS permitted for external ingestion. Disallowed: " + uri.getScheme());
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new SecurityException("URL must specify a valid host: " + urlString);
        }

        String lowerHost = host.toLowerCase();

        // Check host name directly
        if (lowerHost.equals("localhost") || lowerHost.equals("0.0.0.0") || lowerHost.equals("127.0.0.1") || lowerHost.equals("::1")) {
            throw new SecurityException("SSRF Guardrail: Blocked target host " + host);
        }

        for (String suffix : BLOCKED_HOST_SUFFIXES) {
            if (lowerHost.endsWith(suffix)) {
                throw new SecurityException("SSRF Guardrail: Blocked private host domain " + host);
            }
        }

        // Resolve DNS and verify IP address ranges
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
                    throw new SecurityException("SSRF Guardrail: Target resolves to local or private IP address: " + addr.getHostAddress());
                }

                byte[] bytes = addr.getAddress();
                if (bytes.length == 4) {
                    int b0 = bytes[0] & 0xFF;
                    int b1 = bytes[1] & 0xFF;

                    // 10.0.0.0/8
                    if (b0 == 10) throw new SecurityException("SSRF Guardrail: Blocked private IP 10.0.0.0/8");
                    // 172.16.0.0/12
                    if (b0 == 172 && (b1 >= 16 && b1 <= 31)) throw new SecurityException("SSRF Guardrail: Blocked private IP 172.16.0.0/12");
                    // 192.168.0.0/16
                    if (b0 == 192 && b1 == 168) throw new SecurityException("SSRF Guardrail: Blocked private IP 192.168.0.0/16");
                    // 169.254.0.0/16 (Link-Local & Cloud Metadata 169.254.169.254)
                    if (b0 == 169 && b1 == 254) throw new SecurityException("SSRF Guardrail: Blocked cloud metadata IP 169.254.0.0/16");
                }
            }
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify DNS host resolution for " + host + ": " + e.getMessage(), e);
        }
    }
}
