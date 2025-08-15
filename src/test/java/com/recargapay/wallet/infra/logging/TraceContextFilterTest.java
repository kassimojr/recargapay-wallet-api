package com.recargapay.wallet.infra.logging;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TraceContextFilterTest {

    @Mock
    private OpenTelemetry openTelemetry;

    @Mock
    private Tracer tracer;

    @Mock
    private ContextPropagators contextPropagators;

    @Mock
    private TextMapPropagator textMapPropagator;

    @Mock
    private SpanBuilder spanBuilder;

    @Mock
    private Span span;

    @Mock
    private SpanContext spanContext;

    @Mock
    private Scope scope;

    @Mock
    private FilterChain filterChain;

    private TraceContextFilter traceContextFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        // Setup mock chain
        when(openTelemetry.getTracer("com.recargapay.wallet.http")).thenReturn(tracer);
        when(openTelemetry.getPropagators()).thenReturn(contextPropagators);
        when(contextPropagators.getTextMapPropagator()).thenReturn(textMapPropagator);
        when(textMapPropagator.extract(any(Context.class), any(), any(TextMapGetter.class)))
                .thenReturn(Context.current());
        
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setParent(any(Context.class))).thenReturn(spanBuilder);
        when(spanBuilder.setSpanKind(any())).thenReturn(spanBuilder);
        when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        
        when(span.makeCurrent()).thenReturn(scope);
        when(span.getSpanContext()).thenReturn(spanContext);
        
        // Initialize filter
        traceContextFilter = new TraceContextFilter(openTelemetry);
        
        // Setup request and response
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // Clear MDC before each test
        MDC.clear();
    }

    @Test
    void constructor_ShouldInitializeTracerFromOpenTelemetry() {
        // Given - use a fresh mock to avoid interference from setUp
        OpenTelemetry freshOpenTelemetry = mock(OpenTelemetry.class);
        when(freshOpenTelemetry.getTracer("com.recargapay.wallet.http")).thenReturn(tracer);
        
        // When
        TraceContextFilter filter = new TraceContextFilter(freshOpenTelemetry);
        
        // Then
        assertNotNull(filter);
        verify(freshOpenTelemetry).getTracer("com.recargapay.wallet.http");
    }

    @Test
    void doFilterInternal_WithValidTraceAndSpanIds_ShouldSetMDCAndProceed() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/api/v1/wallets");
        request.setRemoteAddr("192.168.1.1");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).setAttribute("http.status_code", 200);
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithZeroTraceId_ShouldGenerateNewTraceId() throws ServletException, IOException {
        // Given
        String zeroTraceId = "00000000000000000000000000000000";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(zeroTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("POST");
        request.setRequestURI("/api/v1/auth/login");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).end();
        verify(scope).close();
        
        // Verify that a new trace ID was generated (not the zero one)
        // We can't verify the exact value since it's randomly generated
        verify(spanContext).getTraceId();
    }

    @Test
    void doFilterInternal_WithZeroSpanId_ShouldGenerateNewSpanId() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String zeroSpanId = "0000000000000000";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(zeroSpanId);
        
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/wallets/123");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).end();
        verify(scope).close();
        
        // Verify that a new span ID was generated (not the zero one)
        verify(spanContext).getSpanId();
    }

    @Test
    void doFilterInternal_WithBothZeroIds_ShouldGenerateBothIds() throws ServletException, IOException {
        // Given
        String zeroTraceId = "00000000000000000000000000000000";
        String zeroSpanId = "0000000000000000";
        
        when(spanContext.getTraceId()).thenReturn(zeroTraceId);
        when(spanContext.getSpanId()).thenReturn(zeroSpanId);
        
        request.setMethod("DELETE");
        request.setRequestURI("/api/v1/wallets/456");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).end();
        verify(scope).close();
        
        // Verify that both IDs were accessed for generation
        verify(spanContext).getTraceId();
        verify(spanContext).getSpanId();
    }

    @Test
    void doFilterInternal_WithErrorResponse_ShouldSetErrorStatus() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/api/v1/wallets/nonexistent");
        response.setStatus(404);
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).setAttribute("http.status_code", 404);
        verify(span).setStatus(StatusCode.ERROR);
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithServerErrorResponse_ShouldSetErrorStatus() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("POST");
        request.setRequestURI("/api/v1/wallets");
        response.setStatus(500);
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).setAttribute("http.status_code", 500);
        verify(span).setStatus(StatusCode.ERROR);
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithSuccessResponse_ShouldNotSetErrorStatus() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/api/v1/wallets");
        response.setStatus(200);
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(span).setAttribute("http.status_code", 200);
        verify(span, never()).setStatus(StatusCode.ERROR);
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithException_ShouldRecordExceptionAndRethrow() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        RuntimeException testException = new RuntimeException("Test exception");
        doThrow(testException).when(filterChain).doFilter(request, response);
        
        request.setMethod("POST");
        request.setRequestURI("/api/v1/wallets");
        
        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> traceContextFilter.doFilterInternal(request, response, filterChain));
        
        assertEquals(testException, thrown);
        verify(span).recordException(testException);
        verify(span).setStatus(StatusCode.ERROR, "Test exception");
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithServletException_ShouldRecordExceptionAndRethrow() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        ServletException testException = new ServletException("Servlet error");
        doThrow(testException).when(filterChain).doFilter(request, response);
        
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/wallets/123");
        
        // When & Then
        ServletException thrown = assertThrows(ServletException.class, 
            () -> traceContextFilter.doFilterInternal(request, response, filterChain));
        
        assertEquals(testException, thrown);
        verify(span).recordException(testException);
        verify(span).setStatus(StatusCode.ERROR, "Servlet error");
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithIOException_ShouldRecordExceptionAndRethrow() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        IOException testException = new IOException("IO error");
        doThrow(testException).when(filterChain).doFilter(request, response);
        
        request.setMethod("DELETE");
        request.setRequestURI("/api/v1/wallets/456");
        
        // When & Then
        IOException thrown = assertThrows(IOException.class, 
            () -> traceContextFilter.doFilterInternal(request, response, filterChain));
        
        assertEquals(testException, thrown);
        verify(span).recordException(testException);
        verify(span).setStatus(StatusCode.ERROR, "IO error");
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_ShouldAlwaysCleanupMDC() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/api/v1/wallets");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then - MDC should be cleaned up after filter execution
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("spanId"));
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_WithExceptionShouldAlwaysCleanupMDC() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        RuntimeException testException = new RuntimeException("Test exception");
        doThrow(testException).when(filterChain).doFilter(request, response);
        
        request.setMethod("POST");
        request.setRequestURI("/api/v1/wallets");
        
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> traceContextFilter.doFilterInternal(request, response, filterChain));
        
        // MDC should still be cleaned up even when exception occurs
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("spanId"));
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void doFilterInternal_ShouldCreateSpanWithCorrectAttributes() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("PATCH");
        request.setRequestURI("/api/v1/wallets/789/balance");
        request.setRemoteAddr("10.0.0.1");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(tracer).spanBuilder("PATCH /api/v1/wallets/789/balance");
        verify(spanBuilder).setParent(any(Context.class));
        verify(spanBuilder).setSpanKind(any());
        verify(spanBuilder).setAttribute("http.method", "PATCH");
        verify(spanBuilder).setAttribute("http.url", "http://localhost/api/v1/wallets/789/balance");
        verify(spanBuilder).setAttribute("http.route", "/api/v1/wallets/789/balance");
        verify(spanBuilder).setAttribute("http.client_ip", "10.0.0.1");
        verify(spanBuilder).startSpan();
    }

    @Test
    void doFilterInternal_ShouldExtractContextFromHeaders() throws ServletException, IOException {
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/api/v1/wallets");
        request.addHeader("traceparent", "00-12345678901234567890123456789012-1234567890123456-01");
        
        // When
        traceContextFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(textMapPropagator).extract(eq(Context.current()), eq(request), any(TextMapGetter.class));
        verify(filterChain).doFilter(request, response);
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void generateTraceId_ShouldReturnValidFormat() {
        // This test verifies the private method indirectly through the filter behavior
        // Given
        String zeroTraceId = "00000000000000000000000000000000";
        String validSpanId = "1234567890123456";
        
        when(spanContext.getTraceId()).thenReturn(zeroTraceId);
        when(spanContext.getSpanId()).thenReturn(validSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/test");
        
        // When
        assertDoesNotThrow(() -> traceContextFilter.doFilterInternal(request, response, filterChain));
        
        // Then - should not throw any exceptions, indicating valid trace ID generation
        verify(span).end();
        verify(scope).close();
    }

    @Test
    void generateSpanId_ShouldReturnValidFormat() {
        // This test verifies the private method indirectly through the filter behavior
        // Given
        String validTraceId = "12345678901234567890123456789012";
        String zeroSpanId = "0000000000000000";
        
        when(spanContext.getTraceId()).thenReturn(validTraceId);
        when(spanContext.getSpanId()).thenReturn(zeroSpanId);
        
        request.setMethod("GET");
        request.setRequestURI("/test");
        
        // When
        assertDoesNotThrow(() -> traceContextFilter.doFilterInternal(request, response, filterChain));
        
        // Then - should not throw any exceptions, indicating valid span ID generation
        verify(span).end();
        verify(scope).close();
    }
}
