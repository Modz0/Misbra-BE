package com.Misbra.Service;

import io.github.bucket4j.*;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Store rate limiters by IP address
    private final Map<String, Bucket> ipRateLimiters = new ConcurrentHashMap<>();

    // Store rate limiters by user ID
    private final Map<String, Bucket> userRateLimiters = new ConcurrentHashMap<>();

    /**
     * Check if the request from the given IP should be rate limited
     * @param request The HTTP request
     * @param forwardedIp X-Forwarded-For header value
     * @return ConsumptionProbe containing rate limit information
     */
    public ConsumptionProbe checkIpRateLimit(HttpServletRequest request, String forwardedIp) {
        String clientIp = extractClientIp(forwardedIp, request);
        Bucket bucket = ipRateLimiters.computeIfAbsent(clientIp, ip -> createIpRateLimiterBucket());
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    /**
     * Check if the request from the given user should be rate limited
     * @param userId The user's ID
     * @return ConsumptionProbe containing rate limit information
     */
    public ConsumptionProbe checkUserRateLimit(String userId) {
        Bucket bucket = userRateLimiters.computeIfAbsent(userId, id -> createUserRateLimiterBucket());
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    /**
     * Extract client IP address from request
     */
    public String extractClientIp(String forwardedIp, HttpServletRequest request) {
        if (forwardedIp != null && !forwardedIp.isEmpty()) {
            return forwardedIp.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Create a new IP-based rate limiter bucket: 5 requests per hour
     */
    private Bucket createIpRateLimiterBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create a new user-based rate limiter bucket: 5 requests per hour
     */
    private Bucket createUserRateLimiterBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Mask IP address for privacy in logs/responses
     */
    public String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }

        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***" + "." + "***";
            }
        }

        return ip.substring(0, Math.min(ip.length(), 8)) + "***";
    }
}