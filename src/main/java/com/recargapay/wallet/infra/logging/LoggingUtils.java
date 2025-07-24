package com.recargapay.wallet.infra.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for structured JSON logging with OpenTelemetry trace correlation
 */
public class LoggingUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a structured log entry in JSON format with trace context
     *
     * @param logger    SLF4J Logger
     * @param operation Operation name
     * @param data      Map with contextual data
     */
    public static void log(Logger logger, String operation, Map<String, Object> data) {
        try {
            // Use LinkedHashMap to ensure traceId and spanId appear first
            Map<String, Object> logEntry = new LinkedHashMap<>();
            
            // Add trace information first
            Span span = Span.current();
            if (span != null && span.getSpanContext().isValid()) {
                logEntry.put("traceId", span.getSpanContext().getTraceId());
                logEntry.put("spanId", span.getSpanContext().getSpanId());
            }
            
            // Add operation and timestamp
            logEntry.put("operation", operation);
            logEntry.put("timestamp", System.currentTimeMillis());
            
            // Add contextual data
            logEntry.putAll(data);
            
            // Add MDC data for additional context
            if (MDC.getCopyOfContextMap() != null) {
                MDC.getCopyOfContextMap().forEach((k, v) -> {
                    if (!logEntry.containsKey(k)) {
                        logEntry.put(k, v);
                    }
                });
            }
            
            // Log as single-line JSON
            logger.info(objectMapper.writeValueAsString(logEntry));
        } catch (Exception e) {
            // Fallback if serialization fails
            logger.info("{}: {} (Error formatting log: {})", operation, data, e.getMessage());
        }
    }
    
    /**
     * Simplified method for logging with key-value pairs
     */
    public static void log(Logger logger, String operation, String key1, Object value1) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(key1, value1);
        log(logger, operation, data);
    }
    
    public static void log(Logger logger, String operation, String key1, Object value1, String key2, Object value2) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(key1, value1);
        data.put(key2, value2);
        log(logger, operation, data);
    }
    
    public static void log(Logger logger, String operation, String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(key1, value1);
        data.put(key2, value2);
        data.put(key3, value3);
        log(logger, operation, data);
    }
    
    public static void log(Logger logger, String operation, String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(key1, value1);
        data.put(key2, value2);
        data.put(key3, value3);
        data.put(key4, value4);
        log(logger, operation, data);
    }
}
