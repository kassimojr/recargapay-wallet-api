package com.digital.wallet.infra.config;

import com.digital.wallet.infra.logging.ApiLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the application
 * Registers interceptors and other web-related configurations
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final ApiLoggingInterceptor apiLoggingInterceptor;
    
    /**
     * Constructor
     *
     * @param apiLoggingInterceptor the API logging interceptor
     */
    public WebMvcConfig(ApiLoggingInterceptor apiLoggingInterceptor) {
        this.apiLoggingInterceptor = apiLoggingInterceptor;
    }
    
    /**
     * Configures interceptors for the application
     * 
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the API logging interceptor for all API paths
        registry.addInterceptor(apiLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
