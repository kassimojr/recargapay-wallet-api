package com.recargapay.wallet.infra.logging;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;

/**
 * Filter that handles distributed tracing context propagation.
 * <p>
 * This filter ensures that trace and span IDs are properly:
 * 1. Extracted from incoming request headers (if present)
 * 2. Created for new requests (if not present in headers)
 * 3. Propagated into MDC for logging
 * 4. Made available throughout the request lifecycle
 * <p>
 * Follows OpenTelemetry W3C Trace Context specification.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Profile("!test")
public class TraceContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    @Autowired
    public TraceContextFilter(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        // Get tracer from injected OpenTelemetry instance
        this.tracer = openTelemetry.getTracer("com.recargapay.wallet.http");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Extract context from request headers using OpenTelemetry propagators
        Context parentContext = openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), request, httpServletRequestGetter);

        // Create span representing this HTTP request
        Span serverSpan = tracer.spanBuilder(request.getMethod() + " " + request.getRequestURI())
                .setParent(parentContext)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("http.method", request.getMethod())
                .setAttribute("http.url", request.getRequestURL().toString())
                .setAttribute("http.route", request.getRequestURI())
                .setAttribute("http.client_ip", request.getRemoteAddr())
                .startSpan();

        try (Scope scope = serverSpan.makeCurrent()) {
            // Extract and store trace and span IDs in MDC for logging
            SpanContext spanContext = serverSpan.getSpanContext();
            String traceId = spanContext.getTraceId();
            String spanId = spanContext.getSpanId();
            
            // If no trace ID is present (all zeros), generate a UUID-based one
            if (traceId.equals("00000000000000000000000000000000")) {
                traceId = generateTraceId();
            }
            
            // If no span ID is present (all zeros), generate a UUID-based one
            if (spanId.equals("0000000000000000")) {
                spanId = generateSpanId();
            }
            
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
            
            log.debug("Request received: {} {}, traceId={}, spanId={}",
                    request.getMethod(), request.getRequestURI(),
                    traceId, spanId);

            try {
                // Continue with the filter chain
                filterChain.doFilter(request, response);
                
                // Record response details
                serverSpan.setAttribute("http.status_code", response.getStatus());
                if (response.getStatus() >= 400) {
                    serverSpan.setStatus(StatusCode.ERROR);
                }
            } catch (Exception e) {
                serverSpan.recordException(e);
                serverSpan.setStatus(StatusCode.ERROR, e.getMessage());
                throw e;
            }
        } finally {
            // Always clean up
            MDC.remove("traceId");
            MDC.remove("spanId");
            serverSpan.end();
        }
    }
    
    /**
     * Generates a valid trace ID (32 hex chars) based on a random UUID
     */
    private String generateTraceId() {
        UUID uuid = UUID.randomUUID();
        return String.format("%016x%016x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }
    
    /**
     * Generates a valid span ID (16 hex chars) based on a random UUID
     */
    private String generateSpanId() {
        UUID uuid = UUID.randomUUID();
        return String.format("%016x", uuid.getMostSignificantBits());
    }

    /**
     * Text map getter for HttpServletRequest
     */
    private static final TextMapGetter<HttpServletRequest> httpServletRequestGetter =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(HttpServletRequest request) {
                    return new HeaderIterable(request);
                }

                @Nullable
                @Override
                public String get(@Nullable HttpServletRequest request, String key) {
                    if (request == null) {
                        return null;
                    }
                    return request.getHeader(key);
                }
            };

    /**
     * Iterable for HttpServletRequest headers
     */
    private static class HeaderIterable implements Iterable<String> {
        private final Enumeration<String> headers;

        HeaderIterable(HttpServletRequest request) {
            this.headers = request.getHeaderNames();
        }

        @Override
        public Iterator<String> iterator() {
            return Collections.list(headers).iterator();
        }
    }
}
