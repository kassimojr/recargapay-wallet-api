package com.recargapay.wallet.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParameterNameDiscovererConfig Tests")
class ParameterNameDiscovererConfigTest {

    private ParameterNameDiscovererConfig config;

    @BeforeEach
    void setUp() {
        config = new ParameterNameDiscovererConfig();
    }

    @Test
    @DisplayName("Should create primary ParameterNameDiscoverer bean")
    void shouldCreatePrimaryParameterNameDiscovererBean() {
        // When
        ParameterNameDiscoverer discoverer = config.primaryParameterNameDiscoverer();

        // Then
        assertThat(discoverer).isNotNull();
        assertThat(discoverer).isInstanceOf(StandardReflectionParameterNameDiscoverer.class);
    }

    @Test
    @DisplayName("Should return StandardReflectionParameterNameDiscoverer instance")
    void shouldReturnStandardReflectionParameterNameDiscovererInstance() {
        // When
        ParameterNameDiscoverer discoverer = config.primaryParameterNameDiscoverer();

        // Then
        assertThat(discoverer).isExactlyInstanceOf(StandardReflectionParameterNameDiscoverer.class);
    }

    @Test
    @DisplayName("Should create new instance each time")
    void shouldCreateNewInstanceEachTime() {
        // When
        ParameterNameDiscoverer discoverer1 = config.primaryParameterNameDiscoverer();
        ParameterNameDiscoverer discoverer2 = config.primaryParameterNameDiscoverer();

        // Then
        assertThat(discoverer1).isNotNull();
        assertThat(discoverer2).isNotNull();
        assertThat(discoverer1).isNotSameAs(discoverer2);
    }
}
