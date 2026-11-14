package com.nexus.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int authLimit;
    private final long windowMillis;
    private final ObjectMapper objectMapper;

    // IP -> RateLimitWindow
    private final Map<String, WindowTracker> requestWindows = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${nexus.security.rate-limit.enabled:true}") boolean enabled,
            @Value("${nexus.security.rate-limit.auth-limit:30}") int authLimit,
            @Value("${nexus.security.rate-limit.window-seconds:60}") int windowSeconds,
            ObjectMapper objectMapper
    ) {
        this.enabled = enabled;
        this.authLimit = authLimit;
        this.windowMillis = windowSeconds * 1000L;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isAuthEndpoint(path)) {
            String clientIp = resolveClientIp(request);
            if (!isAllowed(clientIp)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", String.valueOf(windowMillis / 1000));
                
                ApiResponse<Void> errorResp = ApiResponse.error("Too many authentication attempts. Please try again later.");
                response.getWriter().write(objectMapper.writeValueAsString(errorResp));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String path) {
        return path != null && (path.endsWith("/api/v1/auth/login") || path.endsWith("/api/v1/auth/register"));
    }

    private boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        WindowTracker tracker = requestWindows.compute(ip, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMillis) {
                return new WindowTracker(now);
            }
            return existing;
        });

        int currentCount = tracker.counter.incrementAndGet();

        // Periodic maintenance if map gets large
        if (requestWindows.size() > 5000) {
            cleanupStaleEntries(now);
        }

        return currentCount <= authLimit;
    }

    private void cleanupStaleEntries(long now) {
        requestWindows.entrySet().removeIf(entry -> now - entry.getValue().windowStart > windowMillis * 2);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static class WindowTracker {
        final long windowStart;
        final AtomicInteger counter;

        WindowTracker(long windowStart) {
            this.windowStart = windowStart;
            this.counter = new AtomicInteger(0);
        }
    }
}
