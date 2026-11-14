package com.nexus.discovery.service;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class OpportunityNormalizer {

    private static final Set<String> TRACKING_PARAMS = Set.of(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "ref", "fbclid", "gclid", "source", "trk"
    );

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern INSTRUCTION_OVERRIDE_PATTERN = Pattern.compile("(?i)(system prompt|ignore previous instructions|disregard instructions)");

    private static final Pattern INTERN_PATTERN = Pattern.compile("\\b(intern|internship|internships|interns|summer analyst)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern GSOC_PATTERN = Pattern.compile("\\b(summer of code|gsoc)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HACKATHON_PATTERN = Pattern.compile("\\b(hackathon|hackathons|hack)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FELLOWSHIP_PATTERN = Pattern.compile("\\b(fellowship|fellowships|fellow|fellows)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCHOLARSHIP_PATTERN = Pattern.compile("\\b(scholarship|scholarships|grant|grants|bursary|bursaries)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPEN_SOURCE_PATTERN = Pattern.compile("\\b(open source|open-source|contributor|mentorship|outreachy)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPETITION_PATTERN = Pattern.compile("\\b(competition|competitions|challenge|challenges|contest|contests|olympiad)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitizes untrusted external text: strips HTML tags, neutralizes injection patterns, and trims.
     */
    public String sanitizeText(String text) {
        if (text == null) return "";
        String cleaned = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        cleaned = cleaned.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
        cleaned = INSTRUCTION_OVERRIDE_PATTERN.matcher(cleaned).replaceAll("[FILTERED]");
        return cleaned.replaceAll("\\s+", " ").trim();
    }

    /**
     * Normalizes an application or source URL by stripping tracking parameters, converting host to lowercase,
     * and removing trailing slashes for reliable canonical matching.
     */
    public String canonicalizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return null;
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "https";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath() : "";

            // Remove trailing slash if path length > 1
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            // Filter tracking parameters from query string
            StringBuilder queryBuilder = new StringBuilder();
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                String[] pairs = uri.getQuery().split("&");
                List<String> cleanPairs = new ArrayList<>();
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    String key = idx > 0 ? pair.substring(0, idx) : pair;
                    if (!TRACKING_PARAMS.contains(key.toLowerCase())) {
                        cleanPairs.add(pair);
                    }
                }
                Collections.sort(cleanPairs);
                if (!cleanPairs.isEmpty()) {
                    queryBuilder.append("?").append(String.join("&", cleanPairs));
                }
            }

            String portStr = (port != -1 && port != 80 && port != 443) ? (":" + port) : "";
            return scheme + "://" + host + portStr + path + queryBuilder.toString();
        } catch (Exception e) {
            return rawUrl.trim();
        }
    }

    /**
     * Infers one of the 8 canonical categories from title, job type, and explicit source hints.
     */
    public String inferCategory(String title, String typeOrTag, String fallbackCategory) {
        String combined = ((title != null ? title : "") + " " + (typeOrTag != null ? typeOrTag : ""));

        if (GSOC_PATTERN.matcher(combined).find()) {
            return "Google Summer of Code";
        }
        if (HACKATHON_PATTERN.matcher(combined).find()) {
            return "Hackathons";
        }
        if (FELLOWSHIP_PATTERN.matcher(combined).find()) {
            return "Fellowships";
        }
        if (SCHOLARSHIP_PATTERN.matcher(combined).find()) {
            return "Scholarships";
        }
        if (OPEN_SOURCE_PATTERN.matcher(combined).find()) {
            return "Open-source programs";
        }
        if (COMPETITION_PATTERN.matcher(combined).find()) {
            return "Competitions";
        }
        if (INTERN_PATTERN.matcher(combined).find()) {
            return "Internships";
        }
        if (fallbackCategory != null && !fallbackCategory.isBlank()) {
            return fallbackCategory;
        }
        return "Full-time jobs";
    }
}
