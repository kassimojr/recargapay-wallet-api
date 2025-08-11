package com.recargapay.wallet.infra.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interceptor for logging API requests and responses
 * Handles the logging of HTTP requests at the API layer
 * Follows the JSON structured logging pattern used throughout the application
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Store request start time for later use in afterCompletion
        request.setAttribute("requestStartTime", System.currentTimeMillis());
        
        String userId = getUserIdFromSecurityContext();
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String controllerInfo = getControllerInfo(handler);
        
        // Create data map for structured logging
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("method", requestMethod);
        data.put("path", requestUri);
        data.put("controller", controllerInfo);
        data.put("userId", userId);
        
        // Log request received
        LoggingUtils.log(logger, "API_REQUEST_RECEIVED", data);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        // Calculate processing time
        Long startTime = (Long) request.getAttribute("requestStartTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
        
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        int statusCode = response.getStatus();
        String controllerInfo = getControllerInfo(handler);
        
        // Create data map for structured logging
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("method", requestMethod);
        data.put("path", requestUri);
        data.put("controller", controllerInfo);
        data.put("status", String.valueOf(statusCode));
        data.put("processingTimeMs", String.valueOf(duration));
        
        if (ex != null) {
            // Add exception information if available
            data.put("exception", ex.getClass().getName());
            data.put("message", ex.getMessage());
            LoggingUtils.log(logger, "API_REQUEST_ERROR", data);
        } else {
            LoggingUtils.log(logger, "API_REQUEST_COMPLETED", data);
        }
    }
    
    /**
     * Extracts user ID from security context if available
     *
     * @return user ID or "anonymous" if not authenticated
     */
    private String getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
    
    /**
     * Extracts controller and method name from handler
     *
     * @param handler the handler object
     * @return string representation of controller and method
     */
    private String getControllerInfo(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        }
        return "unknown";
    }
}
