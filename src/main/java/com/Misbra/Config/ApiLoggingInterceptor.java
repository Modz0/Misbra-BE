package com.Misbra.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {
    private static final Logger API_LOGGER = LoggerFactory.getLogger("API_LOGGER");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? sanitizeQueryString(request.getQueryString()) : "";
        String clientIp = getClientIp(request);

        API_LOGGER.info("API Request - Method: {}, URI: {}{}, Client IP: {}", method, uri, queryString, clientIp);
        return true;
    }

    /**
     * Safely masks sensitive data in the query string.
     * Modify this to handle your application's sensitive parameters.
     */
    private String sanitizeQueryString(String queryString) {
        // Example: Mask sensitive parameters like "token"
        return queryString.replaceAll("(token=)[^&]+", "$1****");
    }

    /**
     * Extracts the client IP address, handling cases with proxies or load balancers.
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp.split(",")[0].trim(); // Handles cases with multiple forwarded IPs
    }
}
