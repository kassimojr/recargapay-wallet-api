package com.digital.wallet.infra.config;

import com.digital.wallet.infra.logging.ApiLoggingInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebMvcConfig Tests")
class WebMvcConfigTest {

    @Mock
    private ApiLoggingInterceptor apiLoggingInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    private WebMvcConfig webMvcConfig;

    @BeforeEach
    void setUp() {
        webMvcConfig = new WebMvcConfig(apiLoggingInterceptor);
    }

    @Test
    @DisplayName("Should create WebMvcConfig with ApiLoggingInterceptor")
    void shouldCreateWebMvcConfigWithApiLoggingInterceptor() {
        // Given & When
        WebMvcConfig config = new WebMvcConfig(apiLoggingInterceptor);

        // Then
        // Constructor execution is tested by successful instantiation
        // No assertions needed as constructor doesn't return anything
    }

    @Test
    @DisplayName("Should add API logging interceptor for API paths")
    void shouldAddApiLoggingInterceptorForApiPaths() {
        // Given
        when(interceptorRegistry.addInterceptor(apiLoggingInterceptor))
                .thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns("/api/**"))
                .thenReturn(interceptorRegistration);

        // When
        webMvcConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistry).addInterceptor(apiLoggingInterceptor);
        verify(interceptorRegistration).addPathPatterns("/api/**");
    }

    @Test
    @DisplayName("Should configure interceptors correctly")
    void shouldConfigureInterceptorsCorrectly() {
        // Given
        when(interceptorRegistry.addInterceptor(any(ApiLoggingInterceptor.class)))
                .thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString()))
                .thenReturn(interceptorRegistration);

        // When
        webMvcConfig.addInterceptors(interceptorRegistry);

        // Then
        verify(interceptorRegistry, times(1)).addInterceptor(apiLoggingInterceptor);
        verify(interceptorRegistration, times(1)).addPathPatterns("/api/**");
        verifyNoMoreInteractions(interceptorRegistry, interceptorRegistration);
    }
}
