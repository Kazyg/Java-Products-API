package com.hackerrank.sample.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long start = System.nanoTime();
        String previousId = MDC.get("requestId");
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        boolean failed = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failed = true;
            throw exception;
        } finally {
            // Route templates avoid logging user-supplied paths, query strings and payloads.
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            int status = failed ? 500 : response.getStatus();
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.atLevel(status >= 500 ? org.slf4j.event.Level.ERROR
                    : status >= 400 ? org.slf4j.event.Level.WARN : org.slf4j.event.Level.INFO)
                    .log("http_request method={} route={} status={} durationMs={}",
                            request.getMethod(), route == null ? "unmapped" : route, status, duration);
            if (previousId == null) {
                MDC.remove("requestId");
            } else {
                MDC.put("requestId", previousId);
            }
        }
    }
}
