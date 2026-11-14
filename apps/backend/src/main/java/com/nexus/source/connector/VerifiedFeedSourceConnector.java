package com.nexus.source.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class VerifiedFeedSourceConnector implements OpportunitySourceConnector {

    private static final Logger log = LoggerFactory.getLogger(VerifiedFeedSourceConnector.class);
    public static final String SOURCE_KEY = "os-programs-curated";

    @Override
    public String getSourceKey() {
        return SOURCE_KEY;
    }

    @Override
    public SourceMetadata getMetadata() {
        return new SourceMetadata(
                SOURCE_KEY,
                "Open Source Programs Index",
                "MANUAL",
                "Curated opportunities for student contributors and open-source grant programs",
                "https://summerofcode.withgoogle.com"
        );
    }

    @Override
    public SyncResult fetchOpportunities(SyncRequest request) {
        try {
            // Validate SSRF Guardrail on base URL
            if (!isValidHttpsUrl("https://summerofcode.withgoogle.com")) {
                return SyncResult.failure(SOURCE_KEY, "Invalid URL scheme - strictly HTTPS permitted");
            }

            List<NormalizedOpportunity> list = new ArrayList<>();
            list.add(new NormalizedOpportunity(
                    "OS-CURATED-LFX-2027",
                    "Linux Foundation Mentorship Program (LFX)",
                    "The Linux Foundation",
                    sanitizeText("Work under active maintainers on Linux kernel subsystems, CNCF cloud-native tools, and Hyperledger ecosystems with a dedicated monthly stipend."),
                    "Open-source programs",
                    "OPEN_SOURCE",
                    "Remote",
                    "GLOBAL",
                    true,
                    "Open to software contributors globally. Must pass initial coding evaluation.",
                    List.of("C", "Go", "Linux / Bash", "Git & GitHub"),
                    List.of("Kernel Development", "Kubernetes", "Rust"),
                    "https://mentorship.lfx.linuxfoundation.org",
                    "https://linuxfoundation.org",
                    LocalDate.of(2027, 5, 15),
                    "STRICT",
                    LocalDate.of(2027, 6, 1),
                    LocalDate.of(2027, 8, 31),
                    "OPEN",
                    "{\"source\":\"lfx-mentorship\",\"verified\":true}"
            ));

            return SyncResult.success(SOURCE_KEY, list);
        } catch (Exception e) {
            log.error("Failed to sync from feed source: {}", e.getMessage());
            return SyncResult.failure(SOURCE_KEY, "Failed to synchronize feed: " + e.getMessage());
        }
    }

    /**
     * Validates that the external URL is strictly HTTPS and does not resolve to private/local networks.
     */
    public boolean isValidHttpsUrl(String url) {
        if (url == null || url.isBlank()) return false;
        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())) return false;
            String host = uri.getHost();
            if (host == null) return false;
            String lowerHost = host.toLowerCase();
            if (lowerHost.equals("localhost") || lowerHost.endsWith(".localhost") || lowerHost.endsWith(".local") || lowerHost.endsWith(".internal")) {
                return false;
            }
            if (lowerHost.equals("127.0.0.1") || lowerHost.startsWith("10.") || lowerHost.startsWith("192.168.") || lowerHost.equals("::1") || lowerHost.equals("0.0.0.0")) {
                return false;
            }
            if (lowerHost.startsWith("172.")) {
                String[] parts = lowerHost.split("\\.");
                if (parts.length >= 2) {
                    try {
                        int second = Integer.parseInt(parts[1]);
                        if (second >= 16 && second <= 31) return false;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitizes external text to prevent prompt injection or script execution.
     */
    public String sanitizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "")
                .replaceAll("(?i)(system prompt|ignore previous instructions)", "[FILTERED]")
                .trim();
    }
}
